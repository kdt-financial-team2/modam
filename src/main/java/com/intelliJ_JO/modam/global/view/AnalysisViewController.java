package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalysisViewController {

    @GetMapping("/analysis")
    public String spendingAnalysis() {
        return "domain/analysis/spending-analysis";
    }
}
