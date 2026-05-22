package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SavingsViewController {

    @GetMapping("/savings")
    public String savingsList() {
        return "domain/savings/savings-list";
    }

    @GetMapping("/savings/setup")
    public String savingsGoalSetup() {
        return "domain/savings/savings-goal-setup";
    }
}
