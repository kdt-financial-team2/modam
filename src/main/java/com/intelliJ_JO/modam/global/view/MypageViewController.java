package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MypageViewController {

    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public String mypage() {
        return "domain/mypage/mypage";
    }

    @GetMapping("/theme-settings")
    public String themeSettings(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("currentTheme", myPageService.getTheme(userDetails.getMember().getId()));
        return "domain/mypage/theme-settings";
    }

    @PostMapping("/theme-settings")
    public String saveTheme(@RequestParam String theme,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        myPageService.updateTheme(userDetails.getMember().getId(), theme);
        return "redirect:/theme-settings";
    }
}
