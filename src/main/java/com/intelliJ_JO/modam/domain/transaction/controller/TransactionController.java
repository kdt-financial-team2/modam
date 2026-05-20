package com.intelliJ_JO.modam.domain.transaction.controller;

import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "거래 내역", description = "거래 생성/조회 API")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "거래 생성")
    @PostMapping
    public GlobalResponse<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto request) {
        return GlobalResponse.ok(transactionService.createTransaction(request));
    }

    @Operation(summary = "계좌 거래 목록 조회 (커서 기반 페이지네이션)")
    @GetMapping("/{accountId}")
    public GlobalResponse<List<TransactionResponseDto>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId,
            @RequestParam(defaultValue = "10") int size) {
        return GlobalResponse.ok(transactionService.getTransactions(accountId, lastTransactionId, size));
    }
}
