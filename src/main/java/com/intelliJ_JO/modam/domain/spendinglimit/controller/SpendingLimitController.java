package com.intelliJ_JO.modam.domain.spendinglimit.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitDto;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
import com.intelliJ_JO.modam.domain.spendinglimit.service.SpendingLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spending-limits")
@RequiredArgsConstructor
public class SpendingLimitController {

    private final SpendingLimitService spendingLimitService;

    // =========================
    // 소비 제한 생성
    // =========================
    @PostMapping
    public String saveLimits(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SpendingLimitSaveRequest request
    ) {
        return spendingLimitService.saveSpendingLimits(
                userDetails.getMember().getId(),
                request
        );
    }

    // =========================
    // 소비 제한 수정
    // =========================
    @PutMapping("/update")
    public String updateLimits(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SpendingLimitSaveRequest request
    ) {

        return spendingLimitService.updateSpendingLimits(
                userDetails.getMember().getId(),
                request
        );
    }
}
