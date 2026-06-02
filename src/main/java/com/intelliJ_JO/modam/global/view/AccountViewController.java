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
        // 비로그인 사용자는 로그인 페이지로 리다이렉트
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        // 로그인된 사용자 정보를 본인 정보 확인 화면에 pre-fill
        if (userDetails != null) {
            // 휴대폰 번호 (010-XXXX-XXXX 형식)
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

            // 이메일
            model.addAttribute("userEmail", userDetails.getMember().getEmail());

            // 주소 (기본주소 + 상세주소)
            String address = userDetails.getMember().getAddress();
            String detail  = userDetails.getMember().getAddressDetail();
            String full    = address != null ? address : "";
            if (detail != null && !detail.isBlank()) {
                full = full + " " + detail;
            }
            model.addAttribute("userAddress", full);

// 수정용 데이터
            model.addAttribute("memberId",
                    userDetails.getMember().getId());

            model.addAttribute("zipCode",
                    userDetails.getMember().getZipCode());

            model.addAttribute("address",
                    userDetails.getMember().getAddress());

            model.addAttribute("addressDetail",
                    userDetails.getMember().getAddressDetail());
        }
        return "domain/account/group-account-new";
    }

}
