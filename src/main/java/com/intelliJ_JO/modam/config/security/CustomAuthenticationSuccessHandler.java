package com.intelliJ_JO.modam.config.security;

import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountService accountService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
        boolean hasGroupAccount = accountService.getGroupAccountStatus(member).isHasGroupAccount();
        response.sendRedirect(hasGroupAccount ? "/dashboard" : "/group-account/new");
    }
}
