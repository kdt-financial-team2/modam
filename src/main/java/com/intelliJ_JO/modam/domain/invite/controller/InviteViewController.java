package com.intelliJ_JO.modam.domain.invite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InviteViewController {

    @GetMapping("/invite")
    public String invite() {
        return "domain/onboarding/invite";
    }
}
