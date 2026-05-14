package com.intelliJ_JO.modam.domain.transaction.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 거래 내역 생성: 요청 본문의 거래 정보를 검증 후 새 거래를 등록하고 결과를 반환
    @PostMapping
    public GlobalResponse<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(transactionService.createTransaction(request, userDetails.getMember().getId()));
    }

    // 계좌 거래 목록 조회: 커서 기반 페이지네이션으로 특정 계좌의 거래 내역을 size 개씩 반환
    @GetMapping("/{accountId}")
    public GlobalResponse<List<TransactionResponseDto>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId,
            @RequestParam(defaultValue = "10") int size) {
        return GlobalResponse.ok(transactionService.getTransactions(accountId, lastTransactionId, size));
    }
}
