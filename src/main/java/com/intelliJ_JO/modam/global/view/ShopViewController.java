package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ShopViewController {

    private final ItemService itemService;

    private static final int PAGE_SIZE = 8;

    @GetMapping("/point-shop")
    public String pointShop(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "products") String tab,
            @RequestParam(defaultValue = "1") int historyPage,
            Model model
    ) {
        // 카테고리 목록 (DB의 distinct itemType → 한글 라벨)
        model.addAttribute("categories", itemService.getCategories());
        model.addAttribute("selectedCategory", category);

        // 상품 목록 + 페이지네이션
        List<ItemEntity> allItems = itemService.getItems(category);
        int totalPages = Math.max(1, (int) Math.ceil((double) allItems.size() / PAGE_SIZE));
        int currentPage = Math.max(1, Math.min(page, totalPages));

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, allItems.size());

        List<Map<String, Object>> products = allItems.subList(from, to).stream()
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",        item.getId());
                    m.put("name",      item.getItemName());
                    m.put("pointCost", item.getPrice());
                    m.put("imageUrl",  item.getImgUrl());
                    m.put("image",     "🎁");
                    return m;
                })
                .toList();

        model.addAttribute("products",    products);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages",  totalPages);

        // 포인트 잔액 (추후 인증 연동 시 실제 값으로 교체)
        model.addAttribute("couplePoints", 0);

        // 내역 탭 — 추후 인증 연동
        model.addAttribute("paginatedHistory",  List.of());
        model.addAttribute("historyPage",       1);
        model.addAttribute("historyTotalPages", 1);

        return "domain/shop/point-shop";
    }

    @GetMapping("/point-shop/product/{id}")
    public String pointShopProduct(@PathVariable Long id) {
        return "domain/shop/point-shop-product";
    }

    @GetMapping("/point-shop/purchase/{id}")
    public String pointShopPurchase(@PathVariable Long id) {
        return "domain/shop/point-shop-purchase";
    }

    @GetMapping("/point-shop/complete/{id}")
    public String pointShopComplete(@PathVariable Long id) {
        return "domain/shop/point-shop-complete";
    }
}
