package com.intelliJ_JO.modam.domain.savings.controller;

import com.intelliJ_JO.modam.domain.savings.dto.request.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.response.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.service.SavingsService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping
    public GlobalResponse<Void> createSavings(@Valid @RequestBody SavingsCreateRequestDto requestDto) {
        savingsService.createSavings(requestDto);
        return GlobalResponse.ok("저축 목표가 성공적으로 생성되었습니다.");
    }

    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<SavingsResponseDto>> getSavingsByAccountId(@PathVariable Long accountId) {
        return GlobalResponse.ok(savingsService.getSavingsByAccountId(accountId));
    }
}
