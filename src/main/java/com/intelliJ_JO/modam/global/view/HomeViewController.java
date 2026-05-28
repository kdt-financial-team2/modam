package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.couple.dto.request.CoupleInfoRequestDto;
import com.intelliJ_JO.modam.domain.couple.service.CoupleService;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final DashboardService dashboardService;
    private final CoupleService coupleService;
    private final PointService pointService;

    @GetMapping("/")
    public String landing() {
        return "domain/home/landing";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletResponse response) {
        log.debug("[대시보드] userDetails={}", userDetails != null ? userDetails.getUsername() : "null");
        if (userDetails == null) {
            log.warn("[대시보드] userDetails가 null입니다. 로그인 페이지로 이동합니다.");
            return "redirect:/auth/login";
        }
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

    /**
     * 출석 체크 처리
     * - 오늘 이미 출석한 경우 중복 지급 없이 대시보드로 복귀
     * - 최초 출석 시 100포인트 적립 후 쿼리 파라미터로 성공 여부 전달
     */
    @PostMapping("/dashboard/checkin")
    public String checkin(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            pointService.savePoint(
                    userDetails.getMember().getId(),
                    PointSaveRequest.builder()
                            .reason(PointReason.ATTENDANCE)
                            .amt(100)
                            .descrip("매일 출석 체크 보상")
                            .build()
            );
            return "redirect:/dashboard?checkin=success";
        } catch (IllegalStateException e) {
            // 오늘 이미 출석한 경우
            return "redirect:/dashboard";
        }
    }
}
