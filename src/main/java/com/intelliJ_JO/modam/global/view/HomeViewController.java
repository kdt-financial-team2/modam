package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.couple.dto.request.CoupleInfoRequestDto;
import com.intelliJ_JO.modam.domain.couple.service.CoupleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final DashboardService dashboardService;
    private final CoupleService coupleService;

    @GetMapping("/")
    public String landing() {
        return "domain/home/landing";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        model.addAttribute("userName", userDetails.getMember().getName());
        model.addAttribute("currentPage", "home");
        dashboardService.populate(userDetails.getMember(), model);
        return "domain/home/dashboard";
    }

    /**
     * 대시보드 온보딩 폼에서 커플 시작일 및 계좌 애칭 저장
     * - 저장 후 대시보드로 리다이렉트하면 DashboardService가 저장된 값을 읽어 표시함
     */
    @PostMapping("/dashboard/couple-info")
    public String saveCoupleInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute CoupleInfoRequestDto request) {
        coupleService.updateCoupleInfo(userDetails.getMember(), request.getDDay(), request.getAcctAlias());
        return "redirect:/dashboard";
    }
}
