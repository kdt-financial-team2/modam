package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageViewController {

    @GetMapping("/mypage")
    public String mypage() {
        return "domain/mypage/mypage";
    }

    @GetMapping("/mypage/card/step1")
    public String cardStep1() {
        return "domain/mypage/card-step1";
    }

    @GetMapping("/mypage/card/step2")
    public String cardStep2() {
        return "domain/mypage/card-step2";
    }

    @GetMapping("/mypage/card/step3")
    public String cardStep3() {
        return "domain/mypage/card-step3";
    }

    @GetMapping("/mypage/card/step4")
    public String cardStep4() {
        return "domain/mypage/card-step4";
    }

    @GetMapping("/mypage/card/step5")
    public String cardStep5() {
        return "domain/mypage/card-step5";
    }

    @GetMapping("/mypage/card/step6")
    public String cardStep6() {
        return "domain/mypage/card-step6";
    }

    @GetMapping("/mypage/card/step7")
    public String cardStep7() {
        return "domain/mypage/card-step7";
    }

    @GetMapping("/mypage/card/step8")
    public String cardStep8() {
        return "domain/mypage/card-step8";
    }
}
