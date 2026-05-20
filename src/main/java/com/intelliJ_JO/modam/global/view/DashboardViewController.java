package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardViewController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "domain/dashboard/dashboard";
    }
}
