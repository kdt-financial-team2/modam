package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SpendingViewController {

    private final DashboardService dashboardService;

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
        return "domain/transaction/transaction-history";
    }

    // 대시보드 "최근 소비 내역 > 전체보기" 클릭 시 진입
    @GetMapping("/transaction-history")
    public String transactionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");
        return "domain/transaction/transaction-history";
    }
}
