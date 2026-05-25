package com.intelliJ_JO.modam.domain.transaction.controller;

import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "거래 내역", description = "거래 생성/조회 API")
@Controller
@RequestMapping
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // ===== HTML 페이지 반환 (Thymeleaf용) =====
    @GetMapping("/transactions/{accountId}")
    public String transactionPage(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        List<TransactionResponseDto> transactions =
                transactionService.getTransactions(accountId, lastTransactionId, size);

        // 날짜별 그룹핑
        Map<String, List<TransactionResponseDto>> groupedTransactions = transactions.stream()
                .collect(Collectors.groupingBy(
                        TransactionResponseDto::getDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 총 입금액
        long totalDeposit = transactions.stream()
                .filter(tx -> "deposit".equals(tx.getType()))
                .mapToLong(TransactionResponseDto::getAmount)
                .sum();

        // 총 출금액
        long totalWithdrawal = transactions.stream()
                .filter(tx -> "withdrawal".equals(tx.getType()))
                .mapToLong(TransactionResponseDto::getAmount)
                .sum();

        // 현재 잔액 (마지막 거래의 잔액)
        long currentBalance = transactions.isEmpty() ? 0L
                : transactions.get(transactions.size() - 1).getBalance();

        model.addAttribute("transactions", transactions);
        model.addAttribute("groupedTransactions", groupedTransactions);
        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("totalWithdrawal", totalWithdrawal);
        model.addAttribute("currentBalance", currentBalance);

        return "domain/transaction/transaction-history";
    }

    // ===== REST API (기존 유지) =====
    @ResponseBody
    @PostMapping("/api/transactions")
    @Operation(summary = "거래 생성")
    public GlobalResponse<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto request) {
        return GlobalResponse.ok(transactionService.createTransaction(request));
    }

    @ResponseBody
    @GetMapping("/api/transactions/{accountId}")
    @Operation(summary = "계좌 거래 목록 조회 (커서 기반 페이지네이션)")
    public GlobalResponse<List<TransactionResponseDto>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastTransactionId,
            @RequestParam(defaultValue = "10") int size) {
        return GlobalResponse.ok(transactionService.getTransactions(accountId, lastTransactionId, size));
    }
}