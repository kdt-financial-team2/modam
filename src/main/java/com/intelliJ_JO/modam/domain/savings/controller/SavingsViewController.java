package com.intelliJ_JO.modam.domain.savings.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SavingsViewController {

    @GetMapping("/savings")
    public String savings() {
        return "domain/savings/savings";
    }

    @GetMapping("/savings-goal-setup")
    public String savingsGoalSetup() {
        return "domain/savings/savings-goal-setup";
    }
}
