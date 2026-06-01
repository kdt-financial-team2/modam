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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("couplePoints", pointService.getCurrentPoint(memberId));
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
                                @PathVariable("id") Long id, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        Long memberId = userDetails.getMember().getId();
        model.addAttribute("product", shopService.getProduct(id));
        model.addAttribute("couplePoints", pointService.getCurrentPoint(memberId));
        model.addAttribute("alreadyOwned", shopService.isOwned(memberId, id));
        return "domain/point/product-detail";
    }

    // 구매 확인 페이지 — 이미 보유한 상품이면 안내 페이지로 리다이렉트
    @GetMapping("/point-shop/purchase/{id}")
    public String purchaseConfirm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long id,
                                  Model model, RedirectAttributes redirectAttributes) {
        Long memberId = userDetails.getMember().getId();
        if (shopService.isOwned(memberId, id)) {
            redirectAttributes.addFlashAttribute("product", shopService.getProduct(id));
            return "redirect:/point-shop/already-owned";
        }
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("product", shopService.getProduct(id));
        model.addAttribute("couplePoints", pointService.getCurrentPoint(memberId));
        return "domain/point/purchase";
    }

    // 구매 실행 — 이미 보유한 경우 안내 페이지로, 성공 시 완료 페이지로 리다이렉트
    @PostMapping("/point-shop/purchase/{id}")
    public String executePurchase(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long id,
                                  RedirectAttributes redirectAttributes) {
        Long memberId = userDetails.getMember().getId();
        try {
            int remainingPoints = shopService.purchaseItem(memberId, id);
            redirectAttributes.addFlashAttribute("product", shopService.getProduct(id));
            redirectAttributes.addFlashAttribute("remainingPoints", remainingPoints);
            return "redirect:/point-shop/purchase/complete";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("product", shopService.getProduct(id));
            return "redirect:/point-shop/already-owned";
        }
    }

    @GetMapping("/point-shop/purchase/complete")
    public String purchaseComplete(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/point/purchase-complete";
    }

    // 이미 보유한 상품 안내 페이지
    @GetMapping("/point-shop/already-owned")
    public String alreadyOwned(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/point/already-owned";
    }
}
