package com.intelliJ_JO.modam.domain.spendinglimit.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
import com.intelliJ_JO.modam.domain.spendinglimit.service.SpendingLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/spending-limits")
@RequiredArgsConstructor
public class SpendingLimitController {

    private final SpendingLimitService spendingLimitService;

    @PostMapping
    public void saveLimits(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SpendingLimitSaveRequest request
    ) {

        spendingLimitService.saveSpendingLimits(
                userDetails.getMember().getId(),
                request
        );
    }
}