package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import com.intelliJ_JO.modam.domain.member.service.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MypageProfileViewController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MyPageService myPageService;
    private final DashboardService dashboardService;
    private final AccountMemberRepository accountMemberRepository;

    // [기존 MypageViewController 공통 헬퍼]
    private Member getFreshMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    // [기존 MypageViewController 진입점]
    @GetMapping("/mypage")
    public String mypageRedirect() {
        return "redirect:/mypage/profile";
    }

    // =========================================================================
    // [기존 MypageViewController - 1. 프로필 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);
        model.addAttribute("member", freshMember);
        model.addAttribute("activeMenu", "profile");
        return "domain/mypage/profile";
    }

    @PostMapping("/mypage/profile/update")
    public String updateProfile(
            @RequestParam String name, @RequestParam String phoneNo,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String addressDetail,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();
            memberService.updateMyPageProfile(memberId, name, phoneNo, zipCode, address, addressDetail);
            if (profileImage != null && !profileImage.isEmpty()) {
                myPageService.uploadProfileImage(memberId, profileImage);
            }
            rttr.addFlashAttribute("successMsg", "프로필 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "프로필 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage/profile";
    }

    @PostMapping("/mypage/password/update")
    public String updatePassword(
            @RequestParam String currentPassword, @RequestParam String newPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            memberService.updateMyPagePassword(userDetails.getMember().getId(), currentPassword, newPassword);
            rttr.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/mypage/profile";
    }

    // =========================================================================
    // [기존 MypageViewController - 3. 알림 설정 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/notifications")
    public String notificationsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);
        model.addAttribute("noti", freshMember);
        model.addAttribute("activeMenu", "notifications");
        return "domain/mypage/notifications";
    }

    @PostMapping("/mypage/notifications/update")
    public String updateNotifications(
            @RequestParam(defaultValue = "N") String depositAlert, @RequestParam(defaultValue = "N") String withdrawalAlert,
            @RequestParam(defaultValue = "N") String weeklyReport, @RequestParam(defaultValue = "N") String monthlyReport,
            @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        myPageService.updateNotificationSettings(userDetails.getMember().getId(), depositAlert, withdrawalAlert, weeklyReport, monthlyReport);
        rttr.addFlashAttribute("successMsg", "알림 설정이 성공적으로 저장되었습니다.");
        return "redirect:/mypage/notifications";
    }

    // =========================================================================
    // [기존 MypageViewController - 4. 아이템 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/items")
    public String itemsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);
        model.addAttribute("purchasedThemes", myPageService.getPurchasedThemes(freshMember.getId()));
        model.addAttribute("purchasedEmoticons", myPageService.getPurchasedEmoticons(freshMember.getId()));
        model.addAttribute("activeMenu", "items");
        return "domain/mypage/items";
    }

    @PostMapping("/mypage/items/equip")
    public String equipItem(@RequestParam Long itemId, @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            myPageService.equipItem(userDetails.getMember().getId(), itemId);
            rttr.addFlashAttribute("successMsg", "아이템 장착이 완료되었습니다!");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/mypage/items";
    }

    // =========================================================================
    // [기존 MypageViewController - 7. 회원 탈퇴 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/withdrawal")
    public String withdrawalPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);

        boolean hasActiveAccount = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .anyMatch(am -> am.getInviteStatus() == InviteStatus.ACCEPT
                        && "GROUP".equals(am.getAccount().getAccountType().name())
                        && !"CLOSED".equals(am.getAccount().getStatus().name()));

        model.addAttribute("hasActiveAccount", hasActiveAccount);
        model.addAttribute("activeMenu", "withdrawal");
        return "domain/mypage/withdrawal";
    }

    @PostMapping("/mypage/verify-password")
    @ResponseBody
    public java.util.Map<String, Boolean> verifyPassword(@RequestParam String password, @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isMatch = memberService.verifyPassword(userDetails.getMember().getId(), password);
        return java.util.Map.of("success", isMatch);
    }

    @PostMapping("/mypage/withdraw")
    public String processWithdrawal(@RequestParam String password, @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr, HttpServletRequest request) {
        try {
            Long memberId = userDetails.getMember().getId();

            if (!memberService.verifyPassword(memberId, password)) {
                rttr.addFlashAttribute("errorMsg", "비밀번호 검증에 실패하여 탈퇴가 취소되었습니다.");
                return "redirect:/mypage/withdrawal";
            }

            // 활성 공통 계좌가 남아있으면 탈퇴 차단
            boolean hasActiveGroupAccount = accountMemberRepository.findByMemberId(memberId).stream()
                    .anyMatch(am -> am.getInviteStatus() == InviteStatus.ACCEPT
                            && "GROUP".equals(am.getAccount().getAccountType().name())
                            && !"CLOSED".equals(am.getAccount().getStatus().name()));
            if (hasActiveGroupAccount) {
                rttr.addFlashAttribute("errorMsg", "활성 공통 계좌가 있어 탈퇴할 수 없습니다. 계좌 해지 후 다시 시도해주세요.");
                return "redirect:/mypage/withdrawal";
            }

            memberService.deleteMember(memberId);
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            rttr.addFlashAttribute("successMsg", "회원 탈퇴가 완료되었습니다. 계정 정보는 1년간 안전하게 보관 후 파기됩니다.");
            return "redirect:/";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "탈퇴 처리 도중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/withdrawal";
        }
    }
}