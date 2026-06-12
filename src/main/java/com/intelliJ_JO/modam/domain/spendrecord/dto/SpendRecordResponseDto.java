package com.intelliJ_JO.modam.domain.spendrecord.dto;

import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "소비 기록 응답 DTO")
@Getter
public class SpendRecordResponseDto {

    @Schema(description = "소비 기록 ID", example = "1")
    private Long id;

    @Schema(description = "연결된 거래 ID", example = "10")
    private Long transactionId;

    @Schema(description = "거래 유형 (DEPOSIT / WITHDRAW / PAYMENT)", example = "PAYMENT")
    private String txType;

    @Schema(description = "거래 금액 (원)", example = "15000")
    private Long amount;

    @Schema(description = "거래 후 잔액 (원)", example = "485000")
    private Long afterBalance;

    @Schema(description = "가맹점명", example = "스타벅스 강남점")
    private String merchantName;

    @Schema(description = "카테고리", example = "식비")
    private String category;

    @Schema(description = "이미지 URL", example = "https://example.com/receipt.jpg")
    private String imageUrl;

    @Schema(description = "메모", example = "파트너와 함께한 커피")
    private String memo;

    @Schema(description = "이모티콘", example = "☕")
    private String emoticon;

    @Schema(description = "거래 발생일시")
    private LocalDateTime transactionCreatedAt;

    @Schema(description = "소비 기록 생성일시")
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
