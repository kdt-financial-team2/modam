package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spend.service.SpendingLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class RecordViewController {

    private final SpendingLimitService spendingLimitService;

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
    public String spendingLimit(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("spendingLimits",
                spendingLimitService.getSpendingLimits(userDetails.getMember().getId()));
        return "domain/record/spending-limit";
    }

    @PostMapping("/spending-limit")
    public String saveSpendingLimit(@RequestParam Map<String, String> params,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Long> limits = params.entrySet().stream()
                .filter(e -> e.getKey().startsWith("limit_"))
                .collect(Collectors.toMap(
                        e -> e.getKey().substring(6),
                        e -> {
                            try { return Long.parseLong(e.getValue()); }
                            catch (NumberFormatException ex) { return 0L; }
                        }
                ));
        spendingLimitService.saveSpendingLimits(userDetails.getMember().getId(), limits);
        return "redirect:/spending-limit";
    }
}
