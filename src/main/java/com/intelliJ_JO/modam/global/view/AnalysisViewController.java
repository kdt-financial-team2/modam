package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class AnalysisViewController {

    private final DashboardService dashboardService;
    private final AccountMemberRepository accountMemberRepository;

    @GetMapping({"/analysis", "/spending-analysis"})
    public String spendingAnalysis(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestParam(required = false) String month,
                                   Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);

        Long memberId = userDetails.getMember().getId();
        Account account = accountMemberRepository
                .findFirstByMemberId(memberId)
                .map(am -> am.getAccount())
                .orElse(null);

        Long accountId = account != null ? account.getId() : null;

        String selectedMonth = (month != null && !month.isBlank())
                ? month
                : YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        model.addAttribute("currentPage", "analysis");
        model.addAttribute("accountId", accountId);
        model.addAttribute("selectedMonth", selectedMonth);

        return "domain/analysis/spending-analysis";
    }
}
