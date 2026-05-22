package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String login() {
        return "domain/auth/login";
    }

    @GetMapping("/signup/step1")
    public String signupStep1() {
        return "domain/auth/signup-step1";
    }

    @GetMapping("/signup/step2")
    public String signupStep2() {
        return "domain/auth/signup-step2";
    }

    @GetMapping("/signup/step3")
    public String signupStep3() {
        return "domain/auth/signup-step3";
    }

    @GetMapping("/signup/complete")
    public String signupComplete() {
        return "domain/auth/signup-complete";
    }
}
