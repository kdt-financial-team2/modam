package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.member.dto.MemberCreateRequest;
import com.intelliJ_JO.modam.domain.member.dto.SignupForm;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private static final String SESSION_SIGNUP = "signupForm";

    @GetMapping("/auth/login")
    public String login(HttpSession session, Model model) {
        String loginError = (String) session.getAttribute("loginError");
        if (loginError != null) {
            model.addAttribute("loginError", loginError);
            session.removeAttribute("loginError");
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
    public String signupStep3Submit(@ModelAttribute SignupForm stepForm, HttpSession session, HttpServletRequest request) {
        SignupForm form = getOrCreateForm(session);
        if (form.getUserId() == null) {
            return "redirect:/signup/step1";
        }

        form.setSelectedBank(stepForm.getSelectedBank());
        form.setAccountNumber(stepForm.getAccountNumber());

        MemberCreateRequest memberCreateRequest = MemberCreateRequest.builder()
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

        // 세션 제거 전에 로그인에 필요한 자격증명 저장
        String userId = form.getUserId();
        String rawPassword = form.getPassword();

        memberService.createMember(memberCreateRequest);

        // 기존 세션 무효화 — 이전에 로그인된 다른 계정 정보 제거
        session.invalidate();

        // 신규 가입 회원 자동 로그인 — 이후 /group-account/new에서 올바른 세션 정보 사용
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, rawPassword)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        } catch (Exception e) {
            // 자동 로그인 실패 시 로그인 페이지로 이동
            return "redirect:/auth/login";
        }

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
