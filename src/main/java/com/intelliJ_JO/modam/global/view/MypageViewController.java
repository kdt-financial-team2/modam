package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MypageViewController {

    private final AccountMemberRepository accountMemberRepository;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        model.addAttribute("user", member);

        // GROUP 계좌 조회
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst()
                .orElse(null);

        if (myMembership != null) {
            Account account = myMembership.getAccount();
            Long accountId = account.getId();

            model.addAttribute("accountBalance",  account.getBalance());
            model.addAttribute("accountNumber",   account.getAccountNumber());
            model.addAttribute("hasActiveAccount", true);

            // 파트너 정보
            List<AccountMember> allMembers = accountMemberRepository.findByAccountId(accountId);
            AccountMember partner = allMembers.stream()
                    .filter(am -> !am.getMember().getId().equals(member.getId()))
                    .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("partnerName",    partner != null ? partner.getMember().getName() : "");
            model.addAttribute("myDeposit",      0L);
            model.addAttribute("partnerDeposit", 0L);
            model.addAttribute("myRefund",       account.getBalance() / 2);
            model.addAttribute("partnerRefund",  account.getBalance() / 2);
            model.addAttribute("myContribution",      50.0);
            model.addAttribute("partnerContribution", 50.0);
        } else {
            model.addAttribute("accountBalance",      0L);
            model.addAttribute("accountNumber",       "");
            model.addAttribute("hasActiveAccount",    false);
            model.addAttribute("partnerName",         "");
            model.addAttribute("myDeposit",           0L);
            model.addAttribute("partnerDeposit",      0L);
            model.addAttribute("myRefund",            0L);
            model.addAttribute("partnerRefund",       0L);
            model.addAttribute("myContribution",      0.0);
            model.addAttribute("partnerContribution", 0.0);
        }

        model.addAttribute("section",              "profile");
        model.addAttribute("userPoints",           0);
        model.addAttribute("cardIssued",           false);
        model.addAttribute("cardDesign",           "pink");
        model.addAttribute("cardType",             "domestic");
        model.addAttribute("cardStatus",           "ACTIVE");
        model.addAttribute("accountClosureStatus", "none");
        model.addAttribute("closureRequestedBy",   "");
        model.addAttribute("purchasedThemes",      List.of());
        model.addAttribute("purchasedEmoticons",   List.of());

        return "domain/mypage/mypage";
    }

    @GetMapping("/mypage/card/step1")
    public String cardStep1() { return "domain/mypage/card-step1"; }

    @GetMapping("/mypage/card/step2")
    public String cardStep2() { return "domain/mypage/card-step2"; }

    @GetMapping("/mypage/card/step3")
    public String cardStep3() { return "domain/mypage/card-step3"; }

    @GetMapping("/mypage/card/step4")
    public String cardStep4() { return "domain/mypage/card-step4"; }

    @GetMapping("/mypage/card/step5")
    public String cardStep5() { return "domain/mypage/card-step5"; }

    @GetMapping("/mypage/card/step6")
    public String cardStep6() { return "domain/mypage/card-step6"; }

    @GetMapping("/mypage/card/step7")
    public String cardStep7() { return "domain/mypage/card-step7"; }

    @GetMapping("/mypage/card/step8")
    public String cardStep8() { return "domain/mypage/card-step8"; }
}
