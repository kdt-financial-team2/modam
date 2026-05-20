package com.intelliJ_JO.modam.domain.savings.controller;

import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "저축", description = "저축 목표 생성/조회/납입 API")
@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @Operation(summary = "저축 목표 생성")
    @PostMapping
    public GlobalResponse<Void> createSavings(@Valid @RequestBody SavingsCreateRequestDto requestDto) {
        savingsService.createSavings(requestDto);
        return GlobalResponse.ok("저축 목표가 성공적으로 생성되었습니다.");
    }

    @Operation(summary = "계좌별 저축 목표 목록 조회")
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<SavingsResponseDto>> getSavingsByAccountId(@PathVariable Long accountId) {
        return GlobalResponse.ok(savingsService.getSavingsByAccountId(accountId));
    }

    @Operation(summary = "저축 목표 납입")
    @PatchMapping("/{savingsId}/deposit")
    public GlobalResponse<Void> depositToSavings(
            @PathVariable Long savingsId,
            @RequestParam Long memberId,
            @RequestBody Long amount) {
        savingsService.depositToSavings(savingsId, amount, memberId);
        return GlobalResponse.ok("저축 목표에 성공적으로 납입되었습니다.");
    }
}
