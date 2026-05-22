package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpendingViewController {

    @GetMapping("/spending-limit")
    public String spendingLimit() {
        return "domain/spending/spending-limit";
    }

    @GetMapping("/transaction-history")
    public String transactionHistory() {
        return "domain/transaction/transaction-history";
    }
}
