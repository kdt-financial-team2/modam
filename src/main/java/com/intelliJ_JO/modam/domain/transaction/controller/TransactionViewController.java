package com.intelliJ_JO.modam.domain.transaction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionViewController {

    @GetMapping("/transaction-history")
    public String transactionHistory() {
        return "domain/transaction/transaction-history";
    }

    @GetMapping("/transfer")
    public String transfer() {
        return "domain/transaction/transfer";
    }

    @GetMapping("/transfer-complete")
    public String transferComplete() {
        return "domain/transaction/transfer-complete";
    }

    @GetMapping("/transfer-failed")
    public String transferFailed() {
        return "domain/transaction/transfer-failed";
    }
}
