package com.intelliJ_JO.modam.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

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
