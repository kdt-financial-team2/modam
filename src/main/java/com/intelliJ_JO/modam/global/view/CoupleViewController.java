package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.couple.service.CoupleService;
import com.intelliJ_JO.modam.domain.invite.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CoupleViewController {

    private final CoupleService coupleService;
    private final InviteService inviteService;
    private final AccountService accountService;

    // 1. 초대장 발송 화면 (랜덤 코드 생성 및 화면 표시)
    @GetMapping("/invite")
    public String invite(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // 계좌 보유 여부 확인 (계좌가 없으면 개설 화면으로 튕겨냄)
        GroupAccountStatusDto status = accountService.getGroupAccountStatus(userDetails.getMember());
        if (!status.isHasGroupAccount()) {
            return "redirect:/account-setup";
        }

        // 카톡 공유용 랜덤 초대 코드 생성 또는 조회 후 타임리프 뷰(Model)에 전달
        String inviteCode = inviteService.generateInviteCode(status.getAccountId());
        model.addAttribute("inviteCode", inviteCode);

        return "domain/couple/invite";
    }

    // 2. 파트너 초대 코드 입력 처리 (모달창 POST)
    @PostMapping("/couples/accept")
    public String acceptInvite(
            @RequestParam String inviteCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            // 핵심 비즈니스 로직 호출! (연결 및 포인트 지급)
            coupleService.acceptInviteCode(userDetails.getMember().getId(), inviteCode);

            // 성공 시 대시보드로 이동하며 축하 메시지 전달
            redirectAttributes.addFlashAttribute("successMsg", "파트너와 성공적으로 연결되었으며, 1000 포인트가 지급되었습니다! 🎉");
            return "redirect:/dashboard";

        } catch (Exception e) {
            // 실패 시 다시 계좌 개설 화면으로 돌려보내며 에러 메시지 표시
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/account-setup";
        }
    }

    // 3. 이메일 초대 폼 제출 처리 (실제 메일 발송 대신 UI 성공 처리만)
    @PostMapping("/invite/send")
    public String sendInviteEmail(@RequestParam String partnerEmail, RedirectAttributes redirectAttributes) {
        // (이메일 발송은 UX상 생략하기로 했으므로, 사용자가 폼을 제출하면 성공 알림만 띄워줍니다)
        redirectAttributes.addFlashAttribute("inviteSent", true);
        return "redirect:/invite";
    }
}