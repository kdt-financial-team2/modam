package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoupleViewController {

    @GetMapping("/invite")
    public String invite() {
        return "domain/couple/invite";
    }
}
