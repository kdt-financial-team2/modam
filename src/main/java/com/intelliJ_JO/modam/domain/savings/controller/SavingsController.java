package com.intelliJ_JO.modam.domain.savings.controller;

import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import com.intelliJ_JO.modam.global.response.GlobalResponse; // ✨ 팀 공통 응답 포맷 import
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    // 1. 새로운 저축 목표 생성
    @PostMapping
    public GlobalResponse<Void> createSavings(@Valid @RequestBody SavingsCreateRequestDto requestDto) {
        savingsService.createSavings(requestDto);
        return GlobalResponse.ok("저축 목표가 성공적으로 생성되었습니다.");
    }

    // 2. 특정 계좌의 저축 목표 목록 조회
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<SavingsResponseDto>> getSavingsByAccountId(@PathVariable Long accountId) {
        List<SavingsResponseDto> responseDtos = savingsService.getSavingsByAccountId(accountId);
        return GlobalResponse.ok(responseDtos); // 데이터가 있을 때는 ok() 안에 데이터를 넣어줍니다.
    }

    // 3. 특정 저축 목표에 금액 납입
    // 💡 URL 경로: PATCH "/api/savings/{savingsId}/deposit"
    @PatchMapping("/{savingsId}/deposit")
    public GlobalResponse<Void> depositToSavings(
            @PathVariable Long savingsId,
            @RequestParam Long memberId, // 🔥 누가 저축하는지 받기 위해 추가 (Transaction 기록용)
            @RequestBody Long amount) {

        savingsService.depositToSavings(savingsId, amount, memberId);
        return GlobalResponse.ok("저축 목표에 성공적으로 납입되었습니다.");
    }
}