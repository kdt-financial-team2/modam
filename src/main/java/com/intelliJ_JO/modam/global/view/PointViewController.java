package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.point.dto.response.PointResponse;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PointViewController {

    private final DashboardService dashboardService;
    private final PointService pointService;
    private final ShopService shopService;

    private static final DateTimeFormatter HISTORY_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    @GetMapping("/point-shop")
    public String pointShop(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "point");

        Long memberId = userDetails.getMember().getId();
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        List<PointHistoryView> historyViews = pointService.getPointHistories(memberId)
                .stream()
                .map(h -> toHistoryView(h, oneDayAgo))
                .collect(Collectors.toList());

        model.addAttribute("pointHistory", historyViews);
        model.addAttribute("products", shopService.getProducts(null, 1).getContent());
        return "domain/point/point-shop";
    }

    private PointHistoryView toHistoryView(PointResponse h, LocalDateTime oneDayAgo) {
        boolean isEarn = h.getType() == PointType.SAVE;
        boolean isSpecial = h.getReason() == PointReason.INVITE_SUCCESS
                || h.getReason() == PointReason.SAVINGS_100;
        boolean isNew = h.getCreatedAt() != null && h.getCreatedAt().isAfter(oneDayAgo);
        int amt = Math.abs(h.getAmt());
        String createdAt = h.getCreatedAt() != null
                ? h.getCreatedAt().format(HISTORY_DATE_FMT) : "";

        return PointHistoryView.builder()
                .id(h.getId())
                .type(isEarn ? "EARN" : "USE")
                .amt(amt)
                .aftBal(h.getAftBal())
                .descrip(h.getDescrip())
                .createdAt(createdAt)
                .isSpecial(isSpecial)
                .isNew(isNew)
                .build();
    }

    @GetMapping("/point-shop/product/{id}")
    public String productDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("product", shopService.getProduct(id));
        return "domain/point/product-detail";
    }

    @GetMapping("/point-shop/purchase")
    public String purchase(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/point/purchase";
    }

    @GetMapping("/point-shop/purchase/complete")
    public String purchaseComplete(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/point/purchase-complete";
    }
}
