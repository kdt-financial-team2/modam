package com.intelliJ_JO.modam.domain.savings.controller;

import com.intelliJ_JO.modam.domain.savings.dto.request.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.response.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    // 1. 새로운 저축 목표 생성
    @PostMapping
    public ResponseEntity<GlobalResponse<Void>> createSavings(@Valid @RequestBody SavingsCreateRequestDto requestDto) {
        savingsService.createSavings(requestDto);
        return ResponseEntity.ok(GlobalResponse.ok("저축 목표가 성공적으로 생성되었습니다."));
    }

    // 2. 특정 모임 통장(계좌)의 저축 목표 목록 조회
    // 💡 URL 경로: "/api/savings/account/{accountId}"
    @GetMapping("/account/{accountId}")
    public ResponseEntity<GlobalResponse<List<SavingsResponseDto>>> getSavingsByAccountId(@PathVariable Long accountId) {
        List<SavingsResponseDto> savingsList = savingsService.getSavingsByAccountId(accountId);
        return ResponseEntity.ok(GlobalResponse.ok("저축 목표 목록 조회 성공", savingsList));
    }
}