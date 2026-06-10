package com.intelliJ_JO.modam.domain.spendinglimit.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitDto;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
import com.intelliJ_JO.modam.domain.spendinglimit.service.SpendingLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소비 제한", description = "소비 한도 설정 및 알림 조건 관리 API")
@RestController
@RequestMapping("/api/spending-limits")
@RequiredArgsConstructor
public class SpendingLimitController {

    private final SpendingLimitService spendingLimitService;

    // =========================
    // 소비 제한 생성
    // =========================
    @Operation(summary = "소비 제한 생성", description = "카테고리별 소비 한도와 알림 조건을 설정합니다.")
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
    @Operation(summary = "소비 제한 수정", description = "기존에 설정된 소비 한도와 알림 조건을 수정합니다.")
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
