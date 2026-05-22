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
    private static final String SESSION_SIGNUP = "signupForm";

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 일치하지 않습니다");
        }
        return "domain/auth/login";
    }

    // ===== Step 1: 약관 동의 =====

    @GetMapping("/signup/step1")
    public String signupStep1() {
        return "domain/auth/signup-step1";
    }

    @PostMapping("/signup/step1")
    public String signupStep1Submit(@ModelAttribute SignupForm form, HttpSession session) {
        session.setAttribute(SESSION_SIGNUP, form);
        return "redirect:/signup/step2";
    }

    // ===== Step 2: 개인정보 입력 =====

    @GetMapping("/signup/step2")
    public String signupStep2(HttpSession session, Model model) {
        SignupForm form = getOrCreateForm(session);
        model.addAttribute("signupForm", form);
        return "domain/auth/signup-step2";
    }

    @PostMapping("/signup/step2")
    public String signupStep2Submit(@ModelAttribute SignupForm stepForm, HttpSession session) {
        SignupForm form = getOrCreateForm(session);
        form.setUserId(stepForm.getUserId());
        form.setPassword(stepForm.getPassword());
        form.setPasswordConfirm(stepForm.getPasswordConfirm());
        form.setName(stepForm.getName());
        form.setResidentNumberFront(stepForm.getResidentNumberFront());
        form.setResidentNumberBack(stepForm.getResidentNumberBack());
        form.setEnglishLastName(stepForm.getEnglishLastName());
        form.setEnglishFirstName(stepForm.getEnglishFirstName());
        form.setEmail(stepForm.getEmail());
        form.setPhone(stepForm.getPhone());
        form.setPostalCode(stepForm.getPostalCode());
        form.setAddress(stepForm.getAddress());
        form.setAddressDetail(stepForm.getAddressDetail());
        session.setAttribute(SESSION_SIGNUP, form);
        return "redirect:/signup/step3";
    }

    // ===== Step 3: 계좌 연결 + 최종 저장 =====

    @GetMapping("/signup/step3")
    public String signupStep3(HttpSession session, Model model) {
        SignupForm form = getOrCreateForm(session);
        model.addAttribute("signupForm", form);
        return "domain/auth/signup-step3";
    }

    @PostMapping("/signup/step3")
    public String signupStep3Submit(@ModelAttribute SignupForm stepForm, HttpSession session) {
        SignupForm form = getOrCreateForm(session);
        if (form.getUserId() == null) {
            return "redirect:/signup/step1";
        }

        form.setSelectedBank(stepForm.getSelectedBank());
        form.setAccountNumber(stepForm.getAccountNumber());

        MemberCreateRequest request = MemberCreateRequest.builder()
                .userId(form.getUserId())
                .pw(form.getPassword())
                .pwConfirm(form.getPasswordConfirm())
                .name(form.getName())
                .enLast(form.getEnglishLastName())
                .enFirst(form.getEnglishFirstName())
                .email(form.getEmail())
                .phoneNo(form.getPhone().replaceAll("[^0-9]", ""))
                .zipCode(form.getPostalCode())
                .address(form.getAddress())
                .addressDetail(form.getAddressDetail())
                .bankName(form.getSelectedBank())
                .persAcctNo(form.getAccountNumber())
                .rrn(form.getResidentNumberFront() + form.getResidentNumberBack())
                .agreeAge(form.isAgreeAge())
                .agreeService(form.isAgreeTerms())
                .agreePrivacy(form.isAgreePrivacy())
                .agreeFinance(form.isAgreeFinance())
                .notif(form.isAgreeMarketing())
                .agreeThirdParty(form.isAgreeThirdParty())
                .build();

        memberService.createMember(request);
        session.removeAttribute(SESSION_SIGNUP);
        return "redirect:/signup/complete";
    }

    // ===== 가입 완료 =====

    @GetMapping("/signup/complete")
    public String signupComplete() {
        return "domain/auth/signup-complete";
    }

    private SignupForm getOrCreateForm(HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute(SESSION_SIGNUP);
        return form != null ? form : new SignupForm();
    }
}
