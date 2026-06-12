package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TransferViewController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final DashboardService dashboardService;
    private final SavingsService savingsService;

    @GetMapping("/transfer")
    public String transfer(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }
        AccountResponseDto account = accountService.getAccount(status.getAccountId());
        List<SavingsResponseDto> savingsList = savingsService.getSavingsByAccountId(status.getAccountId());
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentBalance", account.getAvailableBalance());
        model.addAttribute("savingsList", savingsList);
        return "domain/transfer/transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(
            @RequestParam(defaultValue = "external") String transferType,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) Long savingsId,
            @RequestParam(required = false) String goalName,
            @RequestParam Long amount,
            @RequestParam String accountPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        Long memberId = userDetails.getMember().getId();

        Map<String, Object> transferData = new HashMap<>();
        transferData.put("amount", amount);
        transferData.put("transferredAt", LocalDateTime.now());
        transferData.put("transferType", transferType);

        try {
            accountService.verifyAccountPassword(status.getAccountId(), accountPassword);

            if ("savings".equals(transferType)) {
                savingsService.depositToSavings(savingsId, amount, memberId);
                transferData.put("goalName", goalName);
            } else {
                TransactionRequestDto request = new TransactionRequestDto();
                request.setMemberId(memberId);
                request.setAccountId(status.getAccountId());
                request.setTxType(TransactionType.WITHDRAW);
                request.setAmount(amount);
                request.setMerchantName(bankName + " " + accountNumber);
                request.setCategory("이체");
                transactionService.createTransaction(request);
                transferData.put("bankName", bankName);
                transferData.put("accountNumber", accountNumber);
            }

            redirectAttributes.addFlashAttribute("transfer", transferData);
            return "redirect:/transfer/complete";

        } catch (Exception e) {
            AccountResponseDto account = accountService.getAccount(status.getAccountId());
            Map<String, Object> failedData = new HashMap<>();
            failedData.put("reason", e.getMessage());
            failedData.put("amount", amount);
            failedData.put("currentBalance", account.getAvailableBalance());
            redirectAttributes.addFlashAttribute("failedTransfer", failedData);
            redirectAttributes.addFlashAttribute("transfer", transferData);
            return "redirect:/transfer/failed";
        }
    }

    @GetMapping("/transfer/complete")
    public String transferComplete(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (!model.containsAttribute("transfer")) return "redirect:/transfer";

        // 송금 후 최신 그룹 계좌 잔액 조회하여 모델에 추가
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (status.isHasGroupAccount()) {
            AccountResponseDto account = accountService.getAccount(status.getAccountId());
            model.addAttribute("currentBalance", account.getAvailableBalance());
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/transfer/transfer-complete";
    }

    @GetMapping("/transfer/failed")
    public String transferFailed(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (!model.containsAttribute("failedTransfer")) return "redirect:/transfer";
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/transfer/transfer-failed";
    }

}
