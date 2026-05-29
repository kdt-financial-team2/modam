package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.member.dto.MemberCreateRequest;
import com.intelliJ_JO.modam.domain.member.dto.SignupForm;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private static final String SESSION_SIGNUP = "signupForm";

    @GetMapping("/auth/login")
    public String login(HttpSession session, Model model, Authentication authentication) {
        log.debug("[자동로그인 확인] authentication type: {}", authentication != null ? authentication.getClass().getSimpleName() : "null");
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
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

    // ===== 아이디 찾기 =====

    @GetMapping("/auth/find-id")
    public String findIdPage() {
        return "domain/auth/find-id";
    }

    @PostMapping("/auth/find-id")
    public String findId(@RequestParam String name, @RequestParam String email,
                         Model model) {
        try {
            String maskedId = memberService.findUserId(name, email);
            model.addAttribute("maskedId", maskedId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("name", name);
        return "domain/auth/find-id";
    }

    // ===== 비밀번호 찾기 =====

    @GetMapping("/auth/find-password")
    public String findPasswordPage(Model model) {
        model.addAttribute("verified", false);
        return "domain/auth/find-password";
    }

    @PostMapping("/auth/find-password/verify")
    public String verifyForPasswordReset(@RequestParam String userId, @RequestParam String email,
                                         HttpSession session, Model model) {
        boolean valid = memberService.verifyForPasswordReset(userId, email);
        if (!valid) {
            model.addAttribute("error", "입력하신 정보와 일치하는 계정이 없습니다.");
            return "domain/auth/find-password";
        }
        session.setAttribute("resetUserId", userId);
        session.setAttribute("resetEmail", email);
        model.addAttribute("verified", true);
        return "domain/auth/find-password";
    }

    @PostMapping("/auth/find-password/reset")
    public String resetPassword(@RequestParam String newPassword,
                                @RequestParam String newPasswordConfirm,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("resetUserId");
        String email = (String) session.getAttribute("resetEmail");
        if (userId == null || email == null) {
            return "redirect:/auth/find-password";
        }
        try {
            memberService.resetPassword(userId, email, newPassword, newPasswordConfirm);
            session.removeAttribute("resetUserId");
            session.removeAttribute("resetEmail");
            redirectAttributes.addFlashAttribute("resetSuccess", true);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            session.setAttribute("resetPasswordError", e.getMessage());
            return "redirect:/auth/find-password?reset=true";
        }
    }

    private SignupForm getOrCreateForm(HttpSession session) {
        SignupForm form = (SignupForm) session.getAttribute(SESSION_SIGNUP);
        return form != null ? form : new SignupForm();
    }
}
