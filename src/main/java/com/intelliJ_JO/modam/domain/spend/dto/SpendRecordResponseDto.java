package com.intelliJ_JO.modam.domain.spend.dto;

import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SpendRecordResponseDto {

    private Long id;
    private Long transactionId;
    private String imageUrl;
    private String memo;
    private String emoticon;
    private LocalDateTime createdAt;

    public SpendRecordResponseDto(SpendRecord spendRecord) {
        this.id = spendRecord.getId();
        this.transactionId = spendRecord.getTransaction().getId();
        this.imageUrl = spendRecord.getImageUrl();
        this.memo = spendRecord.getMemo();
        this.emoticon = spendRecord.getEmoticon();
        this.createdAt = spendRecord.getCreatedAt();
    }
}
