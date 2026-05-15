package com.intelliJ_JO.modam.domain.transaction.dto;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TransactionResponseDto {
    private Long transactionId;
    private String txType;
    private Long amount;
    private Long afterBalance;
    private String merchantName;
    private String category;
    private LocalDateTime createdAt;

    public TransactionResponseDto(Transaction transaction) {
        this.transactionId = transaction.getId();
        this.txType = transaction.getTxType().name();
        this.amount = transaction.getAmount();
        this.afterBalance = transaction.getAfterBalance();
        this.merchantName = transaction.getMerchantName();
        this.category = transaction.getCategory();
        this.createdAt = transaction.getCreatedAt();
    }
}