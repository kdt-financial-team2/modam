package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.CardIssueSessionDto;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
import com.intelliJ_JO.modam.domain.card.service.CardService;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.global.util.AES256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@SessionAttributes("cardIssueSession")
public class MypageCardViewController {

    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final DashboardService dashboardService;
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final AES256Util aes256Util;

    @ModelAttribute("cardIssueSession")
    public CardIssueSessionDto cardIssueSession() {
        return new CardIssueSessionDto();
    }

    private Member getFreshMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    // =========================================================================
    // [기존 MypageViewController - 5. 카드 관리 파트]
    // =========================================================================
    @GetMapping("/mypage/cards")
    public String cardsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);
        model.addAttribute("userName", freshMember.getName());

        boolean isIssued = false;
        Long accountId = null;
        boolean isAccountClosed = false;

        // 🔥 [버그 픽스] 최신 계좌 가져오기
        AccountMember myMembership = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT && "GROUP".equals(am.getAccount().getAccountType().name()))
                .max(Comparator.comparing(am -> am.getAccount().getId()))
                .orElse(null);

        if (myMembership != null) {
            accountId = myMembership.getAccount().getId();
            isAccountClosed = "CLOSED".equals(myMembership.getAccount().getStatus().name());
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

                // 해지된 계좌라면 카드를 강제 INACTIVE 처리
                if (isAccountClosed) {
                    model.addAttribute("cardStatus", "INACTIVE");
                } else {
                    model.addAttribute("cardStatus", myLatestCard.getStatus().name());
                }

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
    // [기존 MypageViewController - 8. 카드 발급 워크플로우 파트]
    // =========================================================================
    @GetMapping("/mypage/card/step1")
    public String cardStep1(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
                            RedirectAttributes rttr) {
        Member freshMember = getFreshMember(userDetails.getMember().getId());
        populateAccountData(freshMember, model);

        // 공통 계좌(GROUP)가 없으면 카드 발급 불가 — 카드 관리 페이지로 돌려보냄
        boolean hasGroupAccount = accountMemberRepository.findByMemberId(freshMember.getId()).stream()
                .anyMatch(am -> am.getInviteStatus() == InviteStatus.ACCEPT
                        && "GROUP".equals(am.getAccount().getAccountType().name()));
        if (!hasGroupAccount) {
            rttr.addFlashAttribute("errorMsg", "카드 발급은 공통 계좌(모임 통장)가 있어야 합니다. 먼저 모임 통장을 개설하거나 파트너의 초대를 수락해주세요.");
            return "redirect:/mypage/cards";
        }

        return "domain/mypage/card-step1";
    }

    @PostMapping("/mypage/card/step1")
    public String processStep1(@ModelAttribute("cardIssueSession") CardIssueSessionDto sessionDto,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes rttr) {
        try {
            // 공통 계좌(GROUP)만 대상으로 조회 — 개인 계좌는 카드와 연결되지 않음
            Account account = accountMemberRepository.findByMemberId(userDetails.getMember().getId()).stream()
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT
                            && "GROUP".equals(am.getAccount().getAccountType().name()))
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("연결된 공통 계좌가 없습니다."))
                    .getAccount();

            if ("CLOSED".equals(account.getStatus().name())) {
                rttr.addFlashAttribute("errorMsg", "해지된 계좌에서는 새로운 카드를 발급할 수 없습니다.");
                return "redirect:/mypage/cards";
            }

            sessionDto.setTargetAccountId(account.getId());
            return "redirect:/mypage/card/step2";

        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", "공통 계좌를 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/mypage/cards";
        }
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
                    .max(Comparator.comparing(am -> am.getAccount().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("연결된 공동 계좌가 없습니다."))
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
                .max(Comparator.comparing(am -> am.getAccount().getId()))
                .map(am -> am.getAccount().getId()).orElse(null);

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

    // 각 컨트롤러가 독립적으로 작동하도록 헬퍼 메서드 복제 유지
    private void populateAccountData(Member member, Model model) {
        dashboardService.populateHeader(member, model);
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .max(Comparator.comparing(am -> am.getAccount().getId()))
                .orElse(null);

        if (myMembership != null) {
            model.addAttribute("hasActiveAccount", true);
            model.addAttribute("isAccountClosed", "CLOSED".equals(myMembership.getAccount().getStatus().name()));
            model.addAttribute("accountBalance", myMembership.getAccount().getBalance());
            model.addAttribute("accountNumber", myMembership.getAccount().getAccountNumber());
        } else {
            model.addAttribute("hasActiveAccount", false);
            model.addAttribute("isAccountClosed", false);
            model.addAttribute("accountBalance", 0L);
            model.addAttribute("accountNumber", "");
        }
    }
}