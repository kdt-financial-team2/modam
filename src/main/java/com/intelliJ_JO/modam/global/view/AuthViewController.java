package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.member.dto.MemberCreateRequest;
import com.intelliJ_JO.modam.domain.member.dto.SignupForm;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final MemberService memberService;

    @GetMapping("/")
    public String index() {
        return "domain/index";
    }

    @GetMapping("/login")
    public String login() {
        return "domain/auth/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "redirect:/signup/step1";
    }

    @GetMapping("/signup/step1")
    public String signupStep1(Model model) {
        model.addAttribute("currentStep", 1);
        return "domain/auth/signup";
    }

    @PostMapping("/signup/step1")
    public String signupStep1Post(
            @RequestParam(defaultValue = "false") boolean agreeAge,
            @RequestParam(defaultValue = "false") boolean agreeTerms,
            @RequestParam(defaultValue = "false") boolean agreePrivacy,
            @RequestParam(defaultValue = "false") boolean agreeFinance,
            @RequestParam(defaultValue = "false") boolean agreeMarketing,
            HttpSession session) {
        SignupForm form = new SignupForm();
        form.setAgreeAge(agreeAge);
        form.setAgreeTerms(agreeTerms);
        form.setAgreePrivacy(agreePrivacy);
        form.setAgreeFinance(agreeFinance);
        form.setAgreeMarketing(agreeMarketing);
        session.setAttribute("signupForm", form);
        return "redirect:/signup/step2";
    }

    @GetMapping("/signup/step2")
    public String signupStep2(Model model, HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute("signupForm");
        model.addAttribute("signupForm", form != null ? form : new SignupForm());
        model.addAttribute("currentStep", 2);
        return "domain/auth/signup";
    }

    @PostMapping("/signup/step2")
    public String signupStep2Post(@ModelAttribute SignupForm stepData, HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute("signupForm");
        if (form == null) form = new SignupForm();
        form.setUserId(stepData.getUserId());
        form.setPassword(stepData.getPassword());
        form.setPasswordConfirm(stepData.getPasswordConfirm());
        form.setName(stepData.getName());
        form.setResidentNumberFront(stepData.getResidentNumberFront());
        form.setResidentNumberBack(stepData.getResidentNumberBack());
        form.setEnglishLastName(stepData.getEnglishLastName());
        form.setEnglishFirstName(stepData.getEnglishFirstName());
        form.setEmail(stepData.getEmail());
        form.setPhone(stepData.getPhone());
        form.setPostalCode(stepData.getPostalCode());
        form.setAddress(stepData.getAddress());
        form.setAddressDetail(stepData.getAddressDetail());
        session.setAttribute("signupForm", form);
        return "redirect:/signup/step3";
    }

    @GetMapping("/signup/step3")
    public String signupStep3(Model model, HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute("signupForm");
        model.addAttribute("signupForm", form != null ? form : new SignupForm());
        model.addAttribute("currentStep", 3);
        return "domain/auth/signup";
    }

    @PostMapping("/signup/step3")
    public String signupStep3Post(@ModelAttribute SignupForm stepData, HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute("signupForm");
        if (form == null) form = new SignupForm();
        form.setSelectedBank(stepData.getSelectedBank());
        form.setAccountNumber(stepData.getAccountNumber());

        MemberCreateRequest request = MemberCreateRequest.builder()
                .userId(form.getUserId())
                .pw(form.getPassword())
                .pwConfirm(form.getPasswordConfirm())
                .name(form.getName())
                .rrn(form.getResidentNumberFront() + form.getResidentNumberBack())
                .enLast(form.getEnglishLastName())
                .enFirst(form.getEnglishFirstName())
                .email(form.getEmail())
                .phoneNo(form.getPhone())
                .zipCode(form.getPostalCode())
                .address(form.getAddress())
                .addressDetail(form.getAddressDetail())
                .bankName(form.getSelectedBank())
                .persAcctNo(form.getAccountNumber())
                .agreeAge(form.isAgreeAge())
                .agreeService(form.isAgreeTerms())
                .agreePrivacy(form.isAgreePrivacy())
                .agreeFinance(form.isAgreeFinance())
                .notif(form.isAgreeMarketing())
                .agreeThirdParty(false)
                .build();

        memberService.createMember(request);
        session.removeAttribute("signupForm");
        return "redirect:/signup/step4";
    }

    @GetMapping("/signup/step4")
    public String signupStep4(Model model) {
        model.addAttribute("currentStep", 4);
        return "domain/auth/signup";
    }

    @GetMapping("/terms")
    public String terms() {
        return "domain/auth/terms";
    }

    @GetMapping("/account-setup")
    public String accountSetup() {
        return "domain/auth/account-setup";
    }

    @GetMapping("/account-setup-complete")
    public String accountSetupComplete() {
        return "domain/auth/account-setup-complete";
    }
}
