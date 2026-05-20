package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardViewController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("userName", userDetails.getMember().getName());
        model.addAttribute("currentMonth", LocalDate.now().getMonthValue());

        // 지출 관련 기본값
        model.addAttribute("totalExpense", 0L);
        model.addAttribute("expenseByCategory", List.of());
        model.addAttribute("recentTransactions", List.of());
        model.addAttribute("expenseChartJson", "{\"labels\":[],\"data\":[],\"colors\":[]}");

        // 커플/계좌 관련 기본값
        model.addAttribute("coupleInfoSaved", false);
        model.addAttribute("coupleStartDate", "");
        model.addAttribute("daysTogether", 0);
        model.addAttribute("coupleAcctAlias", "");
        model.addAttribute("checkedInToday", false);
        model.addAttribute("partnerConnected", false);
        model.addAttribute("partnerName", "");

        // 저축 관련 기본값
        model.addAttribute("savingsGoalCurrent", 0L);
        model.addAttribute("savingsGoalTarget", 0L);
        model.addAttribute("savingsGoalPercent", 0);
        model.addAttribute("savingsGoalRemaining", 0L);
        model.addAttribute("savingsGoalName", "");

        // 포인트 관련 기본값
        model.addAttribute("couplePoints", 0L);
        model.addAttribute("monthlyPoints", 0L);
        model.addAttribute("pointsProgressPercent", 0);

        // 알림 관련 기본값
        model.addAttribute("notifications", List.of());
        model.addAttribute("unreadNotificationCount", 0);

        return "domain/dashboard/dashboard";
    }
}
