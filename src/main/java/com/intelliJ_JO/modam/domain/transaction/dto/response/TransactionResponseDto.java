package com.intelliJ_JO.modam.domain.transaction.dto.response;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TransactionResponseDto {
    private Long transactionId;
    private String transactionType; // DEPOSIT, WITHDRAW, PAYMENT
    private Long amount;
    private Long afterBalance;
    private String merchantName;
    private String category;
    private LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 생성자
    public TransactionResponseDto(Transaction transaction) {
        this.transactionId = transaction.getId();
        this.transactionType = transaction.getTransactionType();
        this.amount = transaction.getAmount();
        this.afterBalance = transaction.getAfterBalance();
        this.merchantName = transaction.getMerchantName();
        this.category = transaction.getCategory();
        this.createdAt = transaction.getCreatedAt();
    }
}