package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.couple.dto.request.CoupleInfoRequestDto;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.service.CoupleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CoupleViewController {

    private final CoupleService coupleService;
    private final DashboardService dashboardService;

    @GetMapping("/invite")
    public String invite() {
        return "domain/couple/invite";
    }

    /**
     * 커플 정보 수정 폼 — 현재 저장된 시작일·애칭을 입력값으로 채워 표시
     */
    @GetMapping("/couple-info/edit")
    public String editForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Couple couple = coupleService.getCoupleByMember(userDetails.getMember());
        model.addAttribute("dDay",      couple.getDDay());
        model.addAttribute("acctAlias", couple.getAccountAlias());
        // 헤더에 필요한 포인트·알림 데이터 공급
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/couple/couple-info-edit";
    }

    /**
     * 커플 정보 수정 처리 — 저장 후 대시보드로 복귀
     */
    @PostMapping("/couple-info/edit")
    public String edit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute CoupleInfoRequestDto request) {
        coupleService.updateCoupleInfo(userDetails.getMember(), request.getDDay(), request.getAcctAlias());
        return "redirect:/dashboard";
    }
}
