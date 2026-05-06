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

    // 1. 거래 내역 생성 (입금/출금/결제)
    @PostMapping
    public ResponseEntity<GlobalResponse<Void>> createTransaction(@Valid @RequestBody TransactionRequestDto requestDto) {
        transactionService.createTransaction(requestDto);
        return ResponseEntity.ok(GlobalResponse.ok("거래가 성공적으로 처리되었습니다."));
    }

    // 2. 🌟 무한 스크롤 기반 거래 내역 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<GlobalResponse<List<TransactionResponseDto>>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId, // 프론트에서 넘겨주는 마지막으로 본 거래내역 ID
            @RequestParam(defaultValue = "10") int size) {          // 한 번에 가져올 개수 (기본 10개)

        // TODO: TransactionService에 getTransactions 메서드를 만들고 No-offset 쿼리와 연결할 예정
        List<TransactionResponseDto> historyList = null; // 임시 처리 (서비스 로직 연동 시 변경)

        return ResponseEntity.ok(GlobalResponse.ok("거래 내역 조회 성공", historyList));
    }
}