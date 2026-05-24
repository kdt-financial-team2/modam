package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TransferViewController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping("/transfer")
    public String transfer(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }
        AccountResponseDto account = accountService.getAccount(status.getAccountId());
        model.addAttribute("currentBalance", account.getAvailableBalance());
        return "domain/transfer/transfer"; // 조장님이 폴더명도 transaction -> transfer로 변경하셨네요!
    }

    @PostMapping("/transfer")
    public String processTransfer(
            @RequestParam String bankName,
            @RequestParam String accountNumber,
            @RequestParam Long amount,
            @RequestParam String accountPassword, // [추가됨] 비밀번호 파라미터 수신
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());

        try {
            // 1. 비밀번호 검증 (방금 추가한 메서드 호출)
            accountService.verifyAccountPassword(status.getAccountId(), accountPassword);

            // 2. 이체 로직 실행
            TransactionRequestDto request = new TransactionRequestDto();
            request.setMemberId(userDetails.getMember().getId());
            request.setAccountId(status.getAccountId());
            request.setTxType(TransactionType.WITHDRAW);
            request.setAmount(amount);
            request.setMerchantName(bankName + " " + accountNumber);
            request.setCategory("이체");

            transactionService.createTransaction(request);

            // 3. 성공 시 데이터 전달
            Map<String, Object> transferData = new HashMap<>();
            transferData.put("bankName", getKoreanBankName(bankName));
            transferData.put("accountNumber", accountNumber);
            transferData.put("amount", amount);
            redirectAttributes.addFlashAttribute("transfer", transferData);

            return "redirect:/transfer/complete";

        } catch (Exception e) {
            // 실패 시 처리
            AccountResponseDto account = accountService.getAccount(status.getAccountId());
            Map<String, Object> failedData = new HashMap<>();
            failedData.put("reason", e.getMessage());
            failedData.put("amount", amount);
            failedData.put("currentBalance", account.getAvailableBalance());
            redirectAttributes.addFlashAttribute("failedTransfer", failedData);

            return "redirect:/transfer/failed";
        }
    }

    @GetMapping("/transfer/complete")
    public String transferComplete(Model model) {
        if (!model.containsAttribute("transfer")) return "redirect:/transfer";
        return "domain/transfer/transfer-complete";
    }

    @GetMapping("/transfer/failed")
    public String transferFailed(Model model) {
        if (!model.containsAttribute("failedTransfer")) return "redirect:/transfer";
        return "domain/transfer/transfer-failed";
    }

    private String getKoreanBankName(String bankCode) {
        return switch (bankCode) {
            case "kb" -> "국민은행";
            case "shinhan" -> "신한은행";
            case "woori" -> "우리은행";
            case "hana" -> "하나은행";
            case "nh" -> "농협은행";
            case "kakao" -> "카카오뱅크";
            case "toss" -> "토스뱅크";
            default -> "기타은행";
        };
    }
}