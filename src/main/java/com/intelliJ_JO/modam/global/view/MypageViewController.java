package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.CardIssueSessionDto;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
import com.intelliJ_JO.modam.domain.card.service.CardService;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import com.intelliJ_JO.modam.domain.member.service.MyPageService;
import com.intelliJ_JO.modam.global.util.AES256Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@SessionAttributes("cardIssueSession")
public class MypageViewController {

    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final DashboardService dashboardService;
    private final MemberService memberService;
    private final MyPageService myPageService;
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final AES256Util aes256Util;
    private final AccountService accountService;

    @ModelAttribute("cardIssueSession")
    public CardIssueSessionDto cardIssueSession() {
        return new CardIssueSessionDto();
    }

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
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);
        model.addAttribute("member", freshMember);
        model.addAttribute("activeMenu", "profile");
        return "domain/mypage/profile";
    }

    @PostMapping("/mypage/profile/update")
    public String updateProfile(
            @RequestParam String name, @RequestParam String phoneNo,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();
            memberService.updateMyPageProfile(memberId, name, phoneNo);
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
    // 2. 연결 계좌 관리
    // =========================================================================
    @GetMapping("/mypage/accounts")
    public String accountsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("activeMenu", "accounts");
        return "domain/mypage/accounts";
    }

    // =========================================================================
    // 3. 알림 설정 관리
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

        boolean isIssued = false;
        Long accountId = null;

        List<AccountMember> memberships = accountMemberRepository.findByMemberId(freshMember.getId());
        for (AccountMember am : memberships) {
            if (am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name())) {
                accountId = am.getAccount().getId();
                break;
            }
        }

        if (accountId != null) {
            List<Card> accountCards = cardRepository.findByAccountId(accountId);
            Card myLatestCard = accountCards.stream()
                    .filter(c -> c.getMember().getId().equals(freshMember.getId()))
                    .max(Comparator.comparing(Card::getCreatedAt))
                    .orElse(null);

            if (myLatestCard != null) {
                isIssued = true;
                model.addAttribute("cardIssued", true);
                model.addAttribute("cardDesign", myLatestCard.getCardDesign() != null ? myLatestCard.getCardDesign() : "pink");
                model.addAttribute("cardType", myLatestCard.getCardType() != null ? myLatestCard.getCardType() : "domestic");
                model.addAttribute("cardStatus", myLatestCard.getStatus().name());
                model.addAttribute("expiryDate", myLatestCard.getExpiryDate());

                try {
                    String decrypted = aes256Util.decrypt(myLatestCard.getCardNumber());
                    model.addAttribute("fullCardNumber", decrypted.replace("-", " "));
                    model.addAttribute("maskedCardNumber", "**** **** **** " + decrypted.substring(decrypted.length() - 4));
                    String cvv = String.format("%03d", (Math.abs(decrypted.hashCode()) % 900) + 100);
                    model.addAttribute("cardCvv", cvv);
                } catch (Exception e) {
                    model.addAttribute("fullCardNumber", "1234 5678 9012 3456");
                    model.addAttribute("maskedCardNumber", "**** **** **** 3456");
                    model.addAttribute("cardCvv", "123");
                }
            }
        }

        if (!isIssued) {
            model.addAttribute("cardIssued", false);
            model.addAttribute("cardDesign", "pink");
            model.addAttribute("cardType", "domestic");
            model.addAttribute("cardStatus", "ACTIVE");
            model.addAttribute("fullCardNumber", "1234 5678 9012 3456");
            model.addAttribute("maskedCardNumber", "**** **** **** 3456");
            model.addAttribute("expiryDate", "12/29");
            model.addAttribute("cardCvv", "123");
        }

        model.addAttribute("activeMenu", "cards");
        return "domain/mypage/cards";
    }

    // =========================================================================
    // 6. 계좌 해지 관리
    // =========================================================================
    @GetMapping("/mypage/close-account")
    public String closeAccountPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("userName", freshMember.getName());
        model.addAttribute("thisMonthSpend", 0L);
        model.addAttribute("targetSavings", 0L);
        model.addAttribute("activeMenu", "close-account");
        return "domain/mypage/close-account";
    }

    @PostMapping("/mypage/close-account")
    public String processCloseAccount(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();

            Long accountId = accountMemberRepository.findByMemberId(memberId).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            String result = accountService.requestAccountClosure(accountId, memberId);

            if ("CLOSED".equals(result)) {
                rttr.addFlashAttribute("successMsg", "공동 모임 계좌 해지가 정상적으로 완료되었습니다. 정산금이 개인 주계좌로 송금되었습니다.");
                return "redirect:/mypage/accounts";
            } else {
                rttr.addFlashAttribute("successMsg", "파트너에게 해지 동의를 요청했습니다. 파트너가 동의하면 해지가 완료됩니다.");
                return "redirect:/mypage/close-account";
            }

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/close-account";
        }
    }

    @PostMapping("/mypage/close-account/cancel")
    public String cancelCloseAccount(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes rttr) {
        try {
            Long memberId = userDetails.getMember().getId();

            Long accountId = accountMemberRepository.findByMemberId(memberId).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("활성 공동 계좌가 존재하지 않습니다."))
                    .getAccount().getId();

            accountService.cancelAccountClosure(accountId, memberId);
            rttr.addFlashAttribute("successMsg", "계좌 해지 요청을 성공적으로 취소했습니다.");

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage/close-account";
    }

    // =========================================================================
    // 7. 회원 탈퇴 관리
    // =========================================================================
    @GetMapping("/mypage/withdrawal")
    public String withdrawalPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        dashboardService.populateHeader(freshMember, model);

        // 🔥 [수정됨] 계좌가 CLOSED 상태가 "아닐 때만" 탈퇴를 막도록 로직 개조
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

    // =========================================================================
    // 🔥 [수정] 헬퍼 메서드: 계좌의 CLOSED 상태 판별 로직 추가
    // =========================================================================
    private void populateAccountData(Member member, Model model) {
        dashboardService.populateHeader(member, model);
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst().orElse(null);

        if (myMembership != null) {
            Account account = myMembership.getAccount();
            Long accountId = account.getId();
            long totalBalance = account.getBalance();
            boolean isClosed = "CLOSED".equals(account.getStatus().name());

            model.addAttribute("accountBalance", totalBalance);
            model.addAttribute("accountNumber", account.getAccountNumber());
            model.addAttribute("hasActiveAccount", true);
            model.addAttribute("isAccountClosed", isClosed);

            AccountMember partner = accountMemberRepository.findByAccountId(accountId).stream()
                    .filter(am -> !am.getMember().getId().equals(member.getId()))
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                    .findFirst().orElse(null);

            long myDepositAmt = myMembership.getTotalDeposit() != null ? myMembership.getTotalDeposit() : 0L;
            long partnerDepositAmt = (partner != null && partner.getTotalDeposit() != null) ? partner.getTotalDeposit() : 0L;
            long totalDepositAmt = myDepositAmt + partnerDepositAmt;

            model.addAttribute("myDeposit", myDepositAmt);
            model.addAttribute("partnerDeposit", partnerDepositAmt);

            if (partner != null) {
                model.addAttribute("partnerName", partner.getMember().getName());

                String myAgree = myMembership.getAgreeClose();
                String partnerAgree = partner.getAgreeClose();

                if (isClosed) {
                    model.addAttribute("accountClosureStatus", "closed");
                    model.addAttribute("closureRequestedBy", "");
                } else if ("Y".equals(myAgree) && "N".equals(partnerAgree)) {
                    model.addAttribute("accountClosureStatus", "pending");
                    model.addAttribute("closureRequestedBy", member.getName());
                } else if ("N".equals(myAgree) && "Y".equals(partnerAgree)) {
                    model.addAttribute("accountClosureStatus", "pending");
                    model.addAttribute("closureRequestedBy", partner.getMember().getName());
                } else {
                    model.addAttribute("accountClosureStatus", "none");
                    model.addAttribute("closureRequestedBy", "");
                }

                if (totalDepositAmt > 0) {
                    double myRatio = (double) myDepositAmt / totalDepositAmt;
                    double partnerRatio = (double) partnerDepositAmt / totalDepositAmt;
                    long myRefundAmt = (long) Math.floor(totalBalance * myRatio);
                    long partnerRefundAmt = totalBalance - myRefundAmt;

                    model.addAttribute("myContribution", myRatio * 100.0);
                    model.addAttribute("partnerContribution", partnerRatio * 100.0);
                    model.addAttribute("myRefund", myRefundAmt);
                    model.addAttribute("partnerRefund", partnerRefundAmt);
                } else {
                    model.addAttribute("myContribution", 50.0);
                    model.addAttribute("partnerContribution", 50.0);
                    model.addAttribute("myRefund", totalBalance / 2);
                    model.addAttribute("partnerRefund", totalBalance - (totalBalance / 2));
                }
            } else {
                model.addAttribute("partnerName", "연결 대기 중");

                if (isClosed) {
                    model.addAttribute("accountClosureStatus", "closed");
                } else {
                    model.addAttribute("accountClosureStatus", "none");
                }

                model.addAttribute("closureRequestedBy", "");
                model.addAttribute("myContribution", 100.0);
                model.addAttribute("partnerContribution", 0.0);
                model.addAttribute("myRefund", totalBalance);
                model.addAttribute("partnerRefund", 0L);
            }
        } else {
            model.addAttribute("accountBalance", 0L);
            model.addAttribute("accountNumber", "");
            model.addAttribute("hasActiveAccount", false);
            model.addAttribute("isAccountClosed", false);
            model.addAttribute("partnerName", "");
            model.addAttribute("accountClosureStatus", "none");
            model.addAttribute("closureRequestedBy", "");
            model.addAttribute("myDeposit", 0L);
            model.addAttribute("partnerDeposit", 0L);
            model.addAttribute("myRefund", 0L);
            model.addAttribute("partnerRefund", 0L);
            model.addAttribute("myContribution", 0.0);
            model.addAttribute("partnerContribution", 0.0);
        }
    }

    // =========================================================================
    // 8. 카드 발급 워크플로우
    // =========================================================================
    @GetMapping("/mypage/card/step1")
    public String cardStep1(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        return "domain/mypage/card-step1";
    }

    @PostMapping("/mypage/card/step1")
    public String processStep1(@ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("연결된 공동 계좌가 없습니다."))
                .getAccount().getId();
        sessionDto.setTargetAccountId(accountId);
        return "redirect:/mypage/card/step2";
    }

    @GetMapping("/mypage/card/step2") public String cardStep2() { return "domain/mypage/card-step2"; }
    @PostMapping("/mypage/card/step2")
    public String processStep2(@RequestParam String cardDesign, @ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto) {
        sessionDto.setCardDesign(cardDesign);
        return "redirect:/mypage/card/step3";
    }

    @GetMapping("/mypage/card/step3") public String cardStep3() { return "domain/mypage/card-step3"; }
    @PostMapping("/mypage/card/step3")
    public String processStep3(@RequestParam String cardType, @ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto) {
        sessionDto.setCardType(cardType);
        return "redirect:/mypage/card/step4";
    }

    @GetMapping("/mypage/card/step4") public String cardStep4() { return "domain/mypage/card-step4"; }
    @PostMapping("/mypage/card/step4")
    public String processStep4(@ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto) {
        sessionDto.setTermsAgreed(true);
        return "redirect:/mypage/card/step5";
    }

    @GetMapping("/mypage/card/step5") public String cardStep5() { return "domain/mypage/card-step5"; }

    @GetMapping("/mypage/card/step6") public String cardStep6() { return "domain/mypage/card-step6"; }
    @PostMapping("/mypage/card/step6")
    public String processStep6(@RequestParam String cardPassword, @ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto) {
        sessionDto.setCardPassword(cardPassword);
        return "redirect:/mypage/card/step7";
    }

    @GetMapping("/mypage/card/step7")
    public String cardStep7(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", getFreshMember(userDetails.getMember().getId()));
        return "domain/mypage/card-step7";
    }

    @PostMapping("/mypage/card/step7")
    public String processStep7(
            @RequestParam String recipientName, @RequestParam String address, @RequestParam String contactNumber,
            @ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes rttr) {

        try {
            if (sessionDto.getCardPassword() == null || sessionDto.getCardDesign() == null) {
                rttr.addFlashAttribute("errorMsg", "세션이 만료되었습니다. 안전을 위해 처음부터 다시 진행해주세요.");
                return "redirect:/mypage/card/step1";
            }

            sessionDto.setRecipientName(recipientName);
            sessionDto.setShippingAddress(address);
            sessionDto.setContactNumber(contactNumber);

            Long accountId = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("연결된 공동 계좌가 없습니다."))
                    .getAccount().getId();

            List<Card> existingCards = cardRepository.findByAccountId(accountId).stream()
                    .filter(c -> c.getMember().getId().equals(userDetails.getMember().getId()))
                    .collect(Collectors.toList());
            if (!existingCards.isEmpty()) {
                cardRepository.deleteAll(existingCards);
            }

            String randomCardNum = String.format("%04d-%04d-%04d-%04d",
                    (int)(Math.random()*10000), (int)(Math.random()*10000),
                    (int)(Math.random()*10000), (int)(Math.random()*10000));
            java.time.LocalDate now = java.time.LocalDate.now();
            String expiryDate = String.format("%02d/%02d", now.getMonthValue(), (now.getYear() + 5) % 100);

            CardCreateRequestDto requestDto = new CardCreateRequestDto();
            requestDto.setAccountId(accountId);
            requestDto.setMemberId(userDetails.getMember().getId());
            requestDto.setCardNumber(randomCardNum);
            requestDto.setExpiryDate(expiryDate);
            requestDto.setCardDesign(sessionDto.getCardDesign());
            requestDto.setCardType(sessionDto.getCardType());
            requestDto.setPassword(sessionDto.getCardPassword());

            cardService.issueCard(requestDto);

            return "redirect:/mypage/card/step8";

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "카드 발급 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/card/step1";
        }
    }

    @GetMapping("/mypage/card/step8")
    public String cardStep8(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, org.springframework.web.bind.support.SessionStatus status) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());

        Long accountId = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst().map(am -> am.getAccount().getId()).orElse(null);

        if (accountId != null) {
            Card myLatestCard = cardRepository.findByAccountId(accountId).stream()
                    .filter(c -> c.getMember().getId().equals(freshMember.getId()))
                    .max(Comparator.comparing(Card::getCreatedAt))
                    .orElse(null);

            if (myLatestCard != null) {
                model.addAttribute("cardDesign", myLatestCard.getCardDesign() != null ? myLatestCard.getCardDesign() : "pink");
            }
        }

        model.addAttribute("user", freshMember);
        model.addAttribute("cardAddress", freshMember.getAddress() + (freshMember.getAddressDetail() != null ? " " + freshMember.getAddressDetail() : ""));

        status.setComplete();
        return "domain/mypage/card-step8";
    }
}