package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequiredArgsConstructor
public class ShopViewController {

    private final ShopService shopService;
    private final PointService pointService;

    @GetMapping("/point-shop")
    public String pointShop(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int historyPage,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long memberId = userDetails.getMember().getId();
        var products = shopService.getProducts(category, page);
        var history  = shopService.getHistory(memberId, historyPage);

        model.addAttribute("products",          products.getContent());
        model.addAttribute("totalPages",         products.getTotalPages());
        model.addAttribute("currentPage",        page);
        model.addAttribute("categories",         shopService.getCategories());
        model.addAttribute("selectedCategory",   category);
        model.addAttribute("couplePoints",       pointService.getCurrentPoint(memberId));
        model.addAttribute("paginatedHistory",   history.getContent());
        model.addAttribute("historyTotalPages",  history.getTotalPages());
        model.addAttribute("historyPage",        historyPage);

        return "domain/shop/point-shop";
    }

    @GetMapping("/point-shop/product/{id}")
    public String pointShopProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long memberId = userDetails.getMember().getId();
        model.addAttribute("product",      shopService.getProduct(id));
        model.addAttribute("couplePoints", pointService.getCurrentPoint(memberId));

        return "domain/shop/point-shop-product";
    }

    @GetMapping("/point-shop/purchase/{id}")
    public String pointShopPurchase(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long memberId = userDetails.getMember().getId();
        model.addAttribute("product",      shopService.getProduct(id));
        model.addAttribute("couplePoints", pointService.getCurrentPoint(memberId));

        return "domain/shop/point-shop-purchase";
    }

    @PostMapping("/point-shop/purchase/{id}")
    public String purchaseItem(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Long memberId = userDetails.getMember().getId();
        int remainingPoints = shopService.purchaseItem(memberId, id);
        redirectAttributes.addFlashAttribute("remainingPoints", remainingPoints);

        return "redirect:/point-shop/complete/" + id;
    }

    @GetMapping("/point-shop/complete/{id}")
    public String pointShopComplete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long memberId = userDetails.getMember().getId();
        model.addAttribute("product", shopService.getProduct(id));

        // flash attribute로 넘어오지 않은 경우 현재 잔액 조회
        if (!model.containsAttribute("remainingPoints")) {
            model.addAttribute("remainingPoints", pointService.getCurrentPoint(memberId));
        }

        return "domain/shop/point-shop-complete";
    }
}
