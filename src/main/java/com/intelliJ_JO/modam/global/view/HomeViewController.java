package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

    @GetMapping("/")
    public String landing() {
        return "domain/home/landing";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "domain/home/dashboard";
    }
}
