package com.intelliJ_JO.modam.domain.savings.controller;

import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    // 1. 새로운 저축 목표 생성
    @PostMapping
    public ResponseEntity<SavingsResponseDto> createSavings(@Valid @RequestBody SavingsCreateRequestDto requestDto) {
        SavingsResponseDto responseDto = savingsService.createSavings(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 2. 특정 계좌의 저축 목표 목록 조회
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<SavingsResponseDto>> getSavingsByAccountId(@PathVariable Long accountId) {
        List<SavingsResponseDto> responseDtos = savingsService.getSavingsByAccountId(accountId);
        return ResponseEntity.ok(responseDtos);
    }

    // 3. 특정 저축 목표에 금액 납입
    // 💡 URL 경로: PATCH "/api/savings/{savingsId}/deposit"
    @PatchMapping("/{savingsId}/deposit")
    public ResponseEntity<String> depositToSavings(
            @PathVariable Long savingsId,
            @RequestBody Long amount) {

        savingsService.depositToSavings(savingsId, amount);

        return ResponseEntity.ok("저축 목표에 성공적으로 납입되었습니다.");
    }
}