package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import com.intelliJ_JO.modam.domain.member.service.MyPageService;
import com.intelliJ_JO.modam.global.view.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageViewController {

    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository; // 🔥 DB 최신 조회를 위해 추가
    private final DashboardService dashboardService;
    private final MemberService memberService;
    private final MyPageService myPageService;

    // 🔥 세션 캐시 대신 DB 최신 데이터를 가져오는 헬퍼 메서드
    private Member getFreshMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    @GetMapping("/mypage")
    public String mypageRedirect() {
        return "redirect:/mypage/profile";
    }

    // =========================================================================
    // 1. 프로필 관리
    // =========================================================================
    @GetMapping("/mypage/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId()); // 🔥 최신 데이터 조회
        dashboardService.populateHeader(freshMember, model);

        model.addAttribute("member", freshMember);
        model.addAttribute("activeMenu", "profile");
        return "domain/mypage/profile";
    }

    @PostMapping("/mypage/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String phoneNo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {
        try {
            memberService.updateMyPageProfile(userDetails.getMember().getId(), name, phoneNo);
            rttr.addFlashAttribute("successMsg", "프로필 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "프로필 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage/profile";
    }

    @PostMapping("/mypage/password/update")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {
        try {
            memberService.updateMyPagePassword(userDetails.getMember().getId(), currentPassword, newPassword);
            rttr.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/mypage/profile";
    }

    // =========================================================================
    // 2. 연결 계좌
    // =========================================================================
    @GetMapping("/mypage/accounts")
    public String accountsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("activeMenu", "accounts");
        return "domain/mypage/accounts";
    }

    // =========================================================================
    // 3. 알림 설정
    // =========================================================================
    @GetMapping("/mypage/notifications")
    public String notificationsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId()); // 🔥 최신 알림 설정값 조회
        dashboardService.populateHeader(freshMember, model);

        model.addAttribute("noti", freshMember);
        model.addAttribute("activeMenu", "notifications");
        return "domain/mypage/notifications";
    }

    @PostMapping("/mypage/notifications/update")
    public String updateNotifications(
            @RequestParam(defaultValue = "N") String depositAlert,
            @RequestParam(defaultValue = "N") String withdrawalAlert,
            @RequestParam(defaultValue = "N") String weeklyReport,
            @RequestParam(defaultValue = "N") String monthlyReport,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {

        myPageService.updateNotificationSettings(
                userDetails.getMember().getId(), depositAlert, withdrawalAlert, weeklyReport, monthlyReport
        );
        rttr.addFlashAttribute("successMsg", "알림 설정이 성공적으로 저장되었습니다.");
        return "redirect:/mypage/notifications";
    }

    // =========================================================================
    // 4. 아이템 관리
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
    // 5. 카드 관리
    // =========================================================================
    @GetMapping("/mypage/cards")
    public String cardsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);

        model.addAttribute("userName", freshMember.getName());
        model.addAttribute("cardIssued", false);
        model.addAttribute("cardDesign", "pink");
        model.addAttribute("cardType", "domestic");
        model.addAttribute("cardStatus", "ACTIVE");
        model.addAttribute("activeMenu", "cards");
        return "domain/mypage/cards";
    }

    // =========================================================================
    // 6. 계좌 해지
    // =========================================================================
    @GetMapping("/mypage/close-account")
    public String closeAccountPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);

        model.addAttribute("userName", freshMember.getName());
        model.addAttribute("userPoints", 0);
        model.addAttribute("accountClosureStatus", "none");
        model.addAttribute("closureRequestedBy", "");
        model.addAttribute("activeMenu", "close-account");
        return "domain/mypage/close-account";
    }

    // =========================================================================
    // 7. 회원 탈퇴
    // =========================================================================
    @GetMapping("/mypage/withdrawal")
    public String withdrawalPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);

        boolean hasActiveAccount = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .anyMatch(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()));

        model.addAttribute("hasActiveAccount", hasActiveAccount);
        model.addAttribute("activeMenu", "withdrawal");
        return "domain/mypage/withdrawal";
    }

    @PostMapping("/mypage/withdraw")
    public String processWithdrawal(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        rttr.addFlashAttribute("successMsg", "회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
        return "redirect:/";
    }

    // =========================================================================
    // 공통 헬퍼 메서드
    // =========================================================================
    private void populateAccountData(Member member, Model model) {
        dashboardService.populateHeader(member, model);

        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst()
                .orElse(null);

        if (myMembership != null) {
            Account account = myMembership.getAccount();
            Long accountId = account.getId();

            model.addAttribute("accountBalance", account.getBalance());
            model.addAttribute("accountNumber", account.getAccountNumber());
            model.addAttribute("hasActiveAccount", true);

            AccountMember partner = accountMemberRepository.findByAccountId(accountId).stream()
                    .filter(am -> !am.getMember().getId().equals(member.getId()))
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                    .findFirst()
                    .orElse(null);

            model.addAttribute("partnerName", partner != null ? partner.getMember().getName() : "");
            model.addAttribute("myDeposit", 0L);
            model.addAttribute("partnerDeposit", 0L);
            model.addAttribute("myRefund", account.getBalance() / 2);
            model.addAttribute("partnerRefund", account.getBalance() / 2);
            model.addAttribute("myContribution", 50.0);
            model.addAttribute("partnerContribution", 50.0);
        } else {
            model.addAttribute("accountBalance", 0L);
            model.addAttribute("accountNumber", "");
            model.addAttribute("hasActiveAccount", false);
            model.addAttribute("partnerName", "");
            model.addAttribute("myDeposit", 0L);
            model.addAttribute("partnerDeposit", 0L);
            model.addAttribute("myRefund", 0L);
            model.addAttribute("partnerRefund", 0L);
            model.addAttribute("myContribution", 0.0);
            model.addAttribute("partnerContribution", 0.0);
        }
    }

    @GetMapping("/mypage/card/step1") public String cardStep1() { return "domain/mypage/card-step1"; }
    @GetMapping("/mypage/card/step2") public String cardStep2() { return "domain/mypage/card-step2"; }
    @GetMapping("/mypage/card/step3") public String cardStep3() { return "domain/mypage/card-step3"; }
    @GetMapping("/mypage/card/step4") public String cardStep4() { return "domain/mypage/card-step4"; }
    @GetMapping("/mypage/card/step5") public String cardStep5() { return "domain/mypage/card-step5"; }
    @GetMapping("/mypage/card/step6") public String cardStep6() { return "domain/mypage/card-step6"; }
    @GetMapping("/mypage/card/step7") public String cardStep7() { return "domain/mypage/card-step7"; }
    @GetMapping("/mypage/card/step8") public String cardStep8() { return "domain/mypage/card-step8"; }
}