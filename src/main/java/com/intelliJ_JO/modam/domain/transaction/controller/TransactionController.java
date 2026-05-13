package com.intelliJ_JO.modam.domain.transaction.controller;

import com.intelliJ_JO.modam.domain.transaction.dto.request.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.response.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 거래 생성 (입금 / 출금 / 결제)
    @PostMapping
    public ResponseEntity<GlobalResponse<TransactionResponseDto>> createTransaction(
            @Valid @RequestBody TransactionRequestDto request) {
        return ResponseEntity.ok(GlobalResponse.ok(transactionService.createTransaction(request)));
    }

    // 거래 내역 조회 (No-offset 무한 스크롤)
    @GetMapping("/{accountId}")
    public ResponseEntity<GlobalResponse<List<TransactionResponseDto>>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(GlobalResponse.ok(
                transactionService.getTransactions(accountId, lastTransactionId, size)));
    }
}