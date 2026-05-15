package com.intelliJ_JO.modam.domain.spend.dto;

import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SpendRecordResponseDto {

    private Long id;
    private Long transactionId;
    private String txType;
    private Long amount;
    private Long afterBalance;
    private String merchantName;
    private String category;
    private String imageUrl;
    private String memo;
    private String emoticon;
    private LocalDateTime transactionCreatedAt;
    private LocalDateTime createdAt;

    public SpendRecordResponseDto(SpendRecord spendRecord) {
        Transaction tx = spendRecord.getTransaction();
        this.id = spendRecord.getId();
        this.transactionId = tx.getId();
        this.txType = tx.getTxType().name();
        this.amount = tx.getAmount();
        this.afterBalance = tx.getAfterBalance();
        this.merchantName = tx.getMerchantName();
        this.category = tx.getCategory();
        this.imageUrl = spendRecord.getImageUrl();
        this.memo = spendRecord.getMemo();
        this.emoticon = spendRecord.getEmoticon();
        this.transactionCreatedAt = tx.getCreatedAt();
        this.createdAt = spendRecord.getCreatedAt();
    }
}
