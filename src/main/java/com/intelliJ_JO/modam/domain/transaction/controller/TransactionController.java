package com.intelliJ_JO.modam.domain.transaction.controller;

import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
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
    private final AccountService accountService; // 비밀번호 검증을 위한 의존성 주입

    @Operation(summary = "거래 생성")
    @PostMapping
    public GlobalResponse<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto request) {

        // [추가된 핵심 로직] 거래 생성 전 계좌 비밀번호 검증
        // 출금(이체)일 경우에만 비밀번호를 검증하도록 방어 로직 추가
        if (request.getTxType() == TransactionType.WITHDRAW && request.getAccountPassword() != null) {
            accountService.verifyAccountPassword(request.getAccountId(), request.getAccountPassword());
        }

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