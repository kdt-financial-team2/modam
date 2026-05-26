package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountViewController {

    @GetMapping("/group-account/new")
    public String groupAccountNew(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // 로그인된 사용자의 이름과 전화번호를 폼에 pre-fill
        if (userDetails != null) {
            model.addAttribute("userName", userDetails.getMember().getName());

            // 전화번호를 010-XXXX-XXXX 형식으로 변환해서 전달
            String rawPhone = userDetails.getMember().getPhoneNo();
            if (rawPhone != null) {
                String digits = rawPhone.replaceAll("\\D", "");
                String formatted = digits;
                if (digits.length() == 11) {
                    formatted = digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
                } else if (digits.length() == 10) {
                    formatted = digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
                }
                model.addAttribute("userPhone", formatted);
            }
        }
        return "domain/account/group-account-new";
    }
}
