package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CardViewController {

    @GetMapping("/card-step{step:[1-8]}")
    public String cardStep(@PathVariable int step) {
        return "domain/card/card-step" + step;
    }
}
