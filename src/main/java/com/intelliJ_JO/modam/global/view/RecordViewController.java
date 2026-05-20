package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RecordViewController {

    @GetMapping("/consumption-history")
    public String consumptionHistory() {
        return "domain/record/consumption-history";
    }

    @GetMapping("/consumption-upload")
    public String consumptionUpload() {
        return "domain/record/consumption-upload";
    }

    @GetMapping("/consumption-detail/{id}")
    public String consumptionDetail(@PathVariable Long id) {
        return "domain/record/consumption-detail";
    }

    @GetMapping("/spending-analysis")
    public String spendingAnalysis() {
        return "domain/record/spending-analysis";
    }

    @GetMapping("/spending-limit")
    public String spendingLimit() {
        return "domain/record/spending-limit";
    }
}
