package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SavingsViewController {

    private final SavingsService savingsService;
    private final AccountService accountService;
    private final DashboardService dashboardService;

    // ===== 저축 목표 리스트 조회 =====
    @GetMapping("/savings")
    public String savingsList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }

        AccountResponseDto account = accountService.getAccount(status.getAccountId());
        List<SavingsResponseDto> savingsList = savingsService.getSavingsByAccountId(status.getAccountId());

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "savings");
        model.addAttribute("userName", userDetails.getMember().getName());
        model.addAttribute("accountBalance", account.getAvailableBalance());
        model.addAttribute("savingsGoals", savingsList);

        return "domain/savings/savings-list";
    }

    // ===== 저축 목표 설정 페이지 이동 =====
    @GetMapping("/savings-goal-setup")
    public String savingsGoalSetup(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("accountId", status.getAccountId());
        model.addAttribute("userName", userDetails.getMember().getName());
        model.addAttribute("partnerName", "파트너");

        return "domain/savings/savings-goal-setup";
    }

    // ===== 저축 목표 생성 처리 (POST) =====
    @PostMapping("/savings-goal-setup")
    public String createSavingsGoal(
            @ModelAttribute SavingsCreateRequestDto requestDto,
            RedirectAttributes redirectAttributes) {
        try {
            savingsService.createSavings(requestDto);
            redirectAttributes.addFlashAttribute("successMsg", "새로운 저축 목표가 등록되었습니다! 💕");
            return "redirect:/savings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/savings-goal-setup";
        }
    }

    // ===== 자동 이체 설정 처리 (POST) =====
    @PostMapping("/savings/auto-transfer")
    public String setupAutoTransfer(
            @RequestParam Long goalId,
            @RequestParam Long amount,
            @RequestParam String frequency,
            @RequestParam String startDate,
            RedirectAttributes redirectAttributes) {

        try {
            savingsService.updateAutoTransfer(goalId, amount, frequency, startDate);

            redirectAttributes.addFlashAttribute("successMsg", "자동 이체 설정이 완료되었습니다! 💸");
            return "redirect:/savings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "자동 이체 설정 중 오류가 발생했습니다.");
            return "redirect:/savings";
        }
    }
}
