package com.intelliJ_JO.modam.domain.point.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ShopViewController {

    @GetMapping("/point-shop")
    public String pointShop() {
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
