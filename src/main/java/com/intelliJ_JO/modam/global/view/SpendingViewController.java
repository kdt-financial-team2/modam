package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SpendingViewController {

    private final DashboardService dashboardService;
    private final TransactionService transactionService;
    private final AccountMemberRepository accountMemberRepository;

    @GetMapping("/spending-limit")
    public String spendingLimit(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/spending/spending-limit";
    }

    // 헤더 네비게이션 "소비기록" 클릭 시 진입
    @GetMapping("/consumption-history")
    public String consumptionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");
        populateTransactionData(userDetails, model);
        return "domain/transaction/transaction-history";
    }

    // 대시보드 "최근 소비 내역 > 전체보기" 클릭 시 진입
    @GetMapping("/transaction-history")
    public String transactionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");
        populateTransactionData(userDetails, model);
        return "domain/transaction/transaction-history";
    }

    // 거래 데이터를 model에 담는 공통 메서드
    private void populateTransactionData(CustomUserDetails userDetails, Model model) {
        try {
            // 로그인한 멤버의 첫 번째 계좌 가져오기
            Long memberId = userDetails.getMember().getId();
            Account account = accountMemberRepository
                    .findFirstByMemberId(memberId)
                    .map(am -> am.getAccount())
                    .orElse(null);

            if (account == null) {
                model.addAttribute("transactions", List.of());
                model.addAttribute("groupedTransactions", Map.of());
                model.addAttribute("totalDeposit", 0L);
                model.addAttribute("totalWithdrawal", 0L);
                model.addAttribute("currentBalance", 0L);
                return;
            }

            List<TransactionResponseDto> transactions =
                    transactionService.getTransactions(account.getId(), null, 50);

            // 날짜별 그룹핑
            Map<String, List<TransactionResponseDto>> groupedTransactions = transactions.stream()
                    .collect(Collectors.groupingBy(
                            TransactionResponseDto::getDate,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            // 총 입금액
            long totalDeposit = transactions.stream()
                    .filter(tx -> "deposit".equals(tx.getType()))
                    .mapToLong(TransactionResponseDto::getAmount)
                    .sum();

            // 총 출금액
            long totalWithdrawal = transactions.stream()
                    .filter(tx -> "withdrawal".equals(tx.getType()))
                    .mapToLong(TransactionResponseDto::getAmount)
                    .sum();

            // 현재 잔액
            long currentBalance = account.getAvailableBalance();

            model.addAttribute("transactions", transactions);
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("totalDeposit", totalDeposit);
            model.addAttribute("totalWithdrawal", totalWithdrawal);
            model.addAttribute("currentBalance", currentBalance);

        } catch (Exception e) {
            model.addAttribute("transactions", List.of());
            model.addAttribute("groupedTransactions", Map.of());
            model.addAttribute("totalDeposit", 0L);
            model.addAttribute("totalWithdrawal", 0L);
            model.addAttribute("currentBalance", 0L);
        }
    }
}