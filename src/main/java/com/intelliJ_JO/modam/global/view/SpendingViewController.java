package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
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
            // ACCEPT 상태의 GROUP 계좌를 기준으로 거래 내역 조회
            Long memberId = userDetails.getMember().getId();
            // 최근 1년치 조회를 위해 충분히 큰 사이즈로 설정
            List<TransactionResponseDto> transactions =
                    transactionService.getTransactionsByMember(memberId, null, 500);

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

            // 현재 잔액: 최신순 정렬이므로 첫 번째 항목이 가장 최근 잔액
            long currentBalance = transactions.isEmpty() ? 0L
                    : transactions.get(0).getBalance();

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