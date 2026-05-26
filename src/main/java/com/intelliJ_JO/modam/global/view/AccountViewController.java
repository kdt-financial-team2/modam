package com.intelliJ_JO.modam.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountViewController {

    @GetMapping("/group-account/new")
    public String groupAccountNew() {
        return "domain/account/group-account-new";
    }
}
