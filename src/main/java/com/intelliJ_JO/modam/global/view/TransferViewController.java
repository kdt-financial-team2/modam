package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransferViewController {

    @GetMapping("/transfer")
    public String transfer() {
        return "domain/transfer/transfer";
    }

    @GetMapping("/transfer/complete")
    public String transferComplete() {
        return "domain/transfer/transfer-complete";
    }

    @GetMapping("/transfer/failed")
    public String transferFailed() {
        return "domain/transfer/transfer-failed";
    }
}
