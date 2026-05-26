package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PointViewController {

    @GetMapping("/point-shop")
    public String pointShop() {
        return "domain/point/point-shop";
    }

    @GetMapping("/point-shop/product/{id}")
    public String productDetail() {
        return "domain/point/product-detail";
    }

    @GetMapping("/point-shop/purchase")
    public String purchase() {
        return "domain/point/purchase";
    }

    @GetMapping("/point-shop/purchase/complete")
    public String purchaseComplete() {
        return "domain/point/purchase-complete";
    }
}
