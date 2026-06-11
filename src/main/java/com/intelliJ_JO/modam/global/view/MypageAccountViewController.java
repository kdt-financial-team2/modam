package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import com.intelliJ_JO.modam.global.view.dto.SavingsGoalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageAccountViewController {

    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final DashboardService dashboardService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final SavingsRepository savingsRepository;

    // [기존 MypageViewController 공통 헬퍼]
    private Member getFreshMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    // =========================================================================
    // [기존 MypageViewController - 2. 연결 계좌 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/accounts")
    public String accountsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("activeMenu", "accounts");
        return "domain/mypage/accounts";
    }

    @PostMapping("/mypage/accounts/limit")
    public String updateAccountLimit(
            @RequestParam(required = false) Long onceTransferLimit,
            @RequestParam(required = false) Long dailyTransferLimit,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {
        try {
            // 🔥 [버그 픽스] 최신 계좌 조회 적용
            Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            AccountUpdateRequestDto requestDto = new AccountUpdateRequestDto();
            requestDto.setOnceTransferLimit(onceTransferLimit);
            requestDto.setDailyTransferLimit(dailyTransferLimit);

            accountService.updateAccount(accountId, requestDto);
            rttr.addFlashAttribute("successMsg", "이체 한도가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "이체 한도 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage/accounts";
    }

    @PostMapping("/mypage/accounts/password")
    public String updateAccountPassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {
        try {
            Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            accountService.updateAccountPassword(accountId, currentPassword, newPassword);
            rttr.addFlashAttribute("successMsg", "계좌 비밀번호가 안전하게 변경되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "비밀번호 변경 실패: " + e.getMessage());
        }
        return "redirect:/mypage/accounts";
    }

    @PostMapping("/mypage/accounts/verify-password")
    @ResponseBody
    public java.util.Map<String, Boolean> verifyAccountPassword(@RequestParam String password, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            accountService.verifyAccountPassword(accountId, password);
            return java.util.Map.of("success", true);
        } catch (Exception e) {
            return java.util.Map.of("success", false);
        }
    }

    @GetMapping("/mypage/accounts/transactions/download")
    public ResponseEntity<String> downloadTransactionsCsv(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            List<TransactionResponseDto> transactions = transactionService.getTransactions(accountId, null, 1000);
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append('\ufeff');
            csvBuilder.append("거래일자,거래시간,구분,거래처(카테고리),거래금액\n");

            for (TransactionResponseDto tx : transactions) {
                String typeStr = "deposit".equals(tx.getType()) ? "입금" : "출금";
                String place = tx.getMerchant() != null ? tx.getMerchant() : tx.getCategory();
                place = place != null ? place.replace(",", " ") : "";

                csvBuilder.append(tx.getDate()).append(",")
                        .append(tx.getTime()).append(",")
                        .append(typeStr).append(",")
                        .append(place).append(",")
                        .append(tx.getAmount()).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modam_transactions.csv");
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

            return ResponseEntity.ok().headers(headers).body(csvBuilder.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/mypage/accounts/certificate")
    public String accountCertificate(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("userName", freshMember.getName());

        AccountMember myMembership = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                .max(Comparator.comparing(am -> am.getAccount().getId()))
                .orElse(null);

        if (myMembership != null) {
            model.addAttribute("createdAt", myMembership.getAccount().getCreatedAt());
        }
        return "domain/mypage/certificate";
    }

    // =========================================================================
    // [기존 MypageViewController - 6. 계좌 해지 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/close-account")
    public String closeAccountPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("userName", freshMember.getName());

        // 이번 달 지출: 대시보드에서 계산한 totalExpense 재사용
        Long totalExpense = (Long) model.asMap().get("totalExpense");
        model.addAttribute("thisMonthSpend", totalExpense != null ? totalExpense : 0L);

        // 목표 저축: 저축 목표별 현재 저축 금액 합계
        @SuppressWarnings("unchecked")
        List<SavingsGoalDto> savingsGoals = (List<SavingsGoalDto>) model.asMap().get("savingsGoals");
        long targetSavings = savingsGoals != null
                ? savingsGoals.stream().mapToLong(SavingsGoalDto::getCurrentAmount).sum()
                : 0L;
        model.addAttribute("targetSavings", targetSavings);

        model.addAttribute("activeMenu", "close-account");
        return "domain/mypage/close-account";
    }

    @PostMapping("/mypage/close-account")
    public String processCloseAccount(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();
            Long accountId = accountMemberRepository.findByMemberId(memberId).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            String result = accountService.requestAccountClosure(accountId, memberId);
            if ("CLOSED".equals(result)) {
                rttr.addFlashAttribute("successMsg", "공동 모임 계좌 해지가 정상적으로 완료되었습니다. 정산금이 개인 주계좌로 송금되었습니다.");
                return "redirect:/mypage/accounts";
            } else {
                rttr.addFlashAttribute("successMsg", "파트너에게 해지 동의를 요청했습니다. 파트너가 동의하면 해지가 완료됩니다.");
                return "redirect:/mypage/close-account";
            }
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/close-account";
        }
    }

    @PostMapping("/mypage/close-account/cancel")
    public String cancelCloseAccount(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();
            Long accountId = accountMemberRepository.findByMemberId(memberId).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            accountService.cancelAccountClosure(accountId, memberId);
            rttr.addFlashAttribute("successMsg", "계좌 해지 요청을 성공적으로 취소했습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage/close-account";
    }

    // =========================================================================
    // [기존 MypageViewController 공통 헬퍼] - 계좌 데이터 주입
    // 🔥 (수정됨) 무조건 최신 생성된 계좌(.max)를 우선 렌더링하도록 픽스!
    // =========================================================================
    private void populateAccountData(Member member, Model model) {
        // populate()로 totalExpense, savingsGoals 등 전체 데이터 설정
        dashboardService.populate(member, model);
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());

        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .max(Comparator.comparing(am -> am.getAccount().getId()))
                .orElse(null);

        if (myMembership != null) {
            Account account = myMembership.getAccount();
            Long accountId = account.getId();
            long totalBalance = account.getBalance();
            boolean isClosed = "CLOSED".equals(account.getStatus().name());

            model.addAttribute("accountBalance", totalBalance);
            model.addAttribute("accountNumber", account.getAccountNumber());
            model.addAttribute("hasActiveAccount", true);
            model.addAttribute("isAccountClosed", isClosed);
            model.addAttribute("onceLimit", account.getOnceTransferLimit());
            model.addAttribute("dailyLimit", account.getDailyTransferLimit());

            AccountMember partner = accountMemberRepository.findByAccountId(accountId).stream()
                    .filter(am -> !am.getMember().getId().equals(member.getId()))
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                    .findFirst().orElse(null);

            long myDepositAmt = myMembership.getTotalDeposit() != null ? myMembership.getTotalDeposit() : 0L;
            long partnerDepositAmt = (partner != null && partner.getTotalDeposit() != null) ? partner.getTotalDeposit() : 0L;
            long totalDepositAmt = myDepositAmt + partnerDepositAmt;

            model.addAttribute("myDeposit", myDepositAmt);
            model.addAttribute("partnerDeposit", partnerDepositAmt);

            if (partner != null) {
                model.addAttribute("partnerName", partner.getMember().getName());

                String myAgree = myMembership.getAgreeClose();
                String partnerAgree = partner.getAgreeClose();

                if (isClosed) {
                    model.addAttribute("accountClosureStatus", "closed");
                    model.addAttribute("closureRequestedBy", "");
                } else if ("Y".equals(myAgree) && "N".equals(partnerAgree)) {
                    model.addAttribute("accountClosureStatus", "pending");
                    model.addAttribute("closureRequestedBy", member.getName());
                } else if ("N".equals(myAgree) && "Y".equals(partnerAgree)) {
                    model.addAttribute("accountClosureStatus", "pending");
                    model.addAttribute("closureRequestedBy", partner.getMember().getName());
                } else {
                    model.addAttribute("accountClosureStatus", "none");
                    model.addAttribute("closureRequestedBy", "");
                }

                // 저축 환급액: '저축 납입' 트랜잭션을 멤버별로 합산해 실제 기여 비율로 분배
                List<Savings> savingsList = savingsRepository.findByAccountId(accountId);
                long totalSavingsToRefund = savingsList.stream()
                        .mapToLong(Savings::getCurrentAmount).sum();

                long mySavingsDeposit = transactionRepository.sumSavingsDepositByMember(accountId, member.getId());
                long partnerSavingsDeposit = transactionRepository
                        .sumSavingsDepositByMember(accountId, partner.getMember().getId());
                long totalSavingsDeposit = mySavingsDeposit + partnerSavingsDeposit;

                long mySavingsRefund;
                long partnerSavingsRefund;
                if (totalSavingsDeposit > 0) {
                    // 실제 납입 트랜잭션 비율로 분배
                    mySavingsRefund = (long) Math.floor(totalSavingsToRefund * (double) mySavingsDeposit / totalSavingsDeposit);
                    partnerSavingsRefund = totalSavingsToRefund - mySavingsRefund;
                } else if (totalDepositAmt > 0) {
                    // 저축 납입 기록이 없으면 계좌 전체 입금 비율로 fallback
                    mySavingsRefund = (long) Math.floor(totalSavingsToRefund * (double) myDepositAmt / totalDepositAmt);
                    partnerSavingsRefund = totalSavingsToRefund - mySavingsRefund;
                } else {
                    // 둘 다 없으면 50/50
                    mySavingsRefund = totalSavingsToRefund / 2;
                    partnerSavingsRefund = totalSavingsToRefund - mySavingsRefund;
                }

                if (totalDepositAmt > 0) {
                    double myRatio = (double) myDepositAmt / totalDepositAmt;
                    double partnerRatio = (double) partnerDepositAmt / totalDepositAmt;
                    long myBalanceRefund = (long) Math.floor(totalBalance * myRatio);
                    model.addAttribute("myContribution", myRatio * 100.0);
                    model.addAttribute("partnerContribution", partnerRatio * 100.0);
                    model.addAttribute("myRefund", myBalanceRefund + mySavingsRefund);
                    model.addAttribute("partnerRefund", (totalBalance - myBalanceRefund) + partnerSavingsRefund);
                } else {
                    model.addAttribute("myContribution", 50.0);
                    model.addAttribute("partnerContribution", 50.0);
                    model.addAttribute("myRefund", (totalBalance / 2) + mySavingsRefund);
                    model.addAttribute("partnerRefund", (totalBalance - (totalBalance / 2)) + partnerSavingsRefund);
                }
                model.addAttribute("mySavingsRefund", mySavingsRefund);
                model.addAttribute("partnerSavingsRefund", partnerSavingsRefund);
            } else {
                model.addAttribute("partnerName", "연결 대기 중");
                model.addAttribute("accountClosureStatus", isClosed ? "closed" : "none");
                model.addAttribute("closureRequestedBy", "");
                List<Savings> savingsListSingle = savingsRepository.findByAccountId(accountId);
                long totalSavingsSingle = savingsListSingle.stream()
                        .mapToLong(Savings::getCurrentAmount).sum();
                model.addAttribute("myContribution", 100.0);
                model.addAttribute("partnerContribution", 0.0);
                model.addAttribute("myRefund", totalBalance + totalSavingsSingle);
                model.addAttribute("partnerRefund", 0L);
                model.addAttribute("mySavingsRefund", totalSavingsSingle);
                model.addAttribute("partnerSavingsRefund", 0L);
            }
        } else {
            model.addAttribute("accountBalance", 0L);
            model.addAttribute("accountNumber", "");
            model.addAttribute("hasActiveAccount", false);
            model.addAttribute("isAccountClosed", false);
            model.addAttribute("partnerName", "");
            model.addAttribute("accountClosureStatus", "none");
            model.addAttribute("closureRequestedBy", "");
            model.addAttribute("myDeposit", 0L);
            model.addAttribute("partnerDeposit", 0L);
            model.addAttribute("myRefund", 0L);
            model.addAttribute("partnerRefund", 0L);
            model.addAttribute("myContribution", 0.0);
            model.addAttribute("partnerContribution", 0.0);
            model.addAttribute("onceLimit", null);
            model.addAttribute("dailyLimit", null);
        }
    }
}