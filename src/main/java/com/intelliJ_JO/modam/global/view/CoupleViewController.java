package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.couple.dto.request.CoupleInfoRequestDto;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.service.CoupleService;
import com.intelliJ_JO.modam.domain.invite.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CoupleViewController {

    private final CoupleService coupleService;
    private final DashboardService dashboardService;
    private final InviteService inviteService;
    private final AccountService accountService;

    // 1. 초대장 발송 화면 (랜덤 코드 생성 및 화면 표시)
    @GetMapping("/invite")
    public String invite(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }

        String inviteCode = inviteService.generateInviteCode(status.getAccountId());
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("inviteCode", inviteCode);

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

    // 2. 파트너 초대 코드 입력 처리 (모달창 POST)
    @PostMapping("/couples/accept")
    public String acceptInvite(
            @RequestParam String inviteCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            coupleService.acceptInviteCode(userDetails.getMember().getId(), inviteCode);
            redirectAttributes.addFlashAttribute("successMsg", "파트너와 성공적으로 연결되었으며, 1000 포인트가 지급되었습니다! 🎉");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/account-setup";
        }
    }

    // 3. 이메일 초대 폼 제출 처리
    @PostMapping("/invite/send")
    public String sendInviteEmail(
            @RequestParam String partnerEmail,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
            if (!status.isHasGroupAccount()) {
                redirectAttributes.addFlashAttribute("inviteError", "모임통장이 없습니다.");
                return "redirect:/invite";
            }
            inviteService.sendInviteCodeByEmail(status.getAccountId(), partnerEmail);
            redirectAttributes.addFlashAttribute("inviteSent", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("inviteError", "이메일 발송에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/invite";
    }
}
