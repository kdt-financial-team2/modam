package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageViewController {

    @GetMapping("/mypage")
    public String mypage() {
        return "domain/mypage/mypage";
    }

    @GetMapping("/theme-settings")
    public String themeSettings() {
        return "domain/mypage/theme-settings";
    }
}
