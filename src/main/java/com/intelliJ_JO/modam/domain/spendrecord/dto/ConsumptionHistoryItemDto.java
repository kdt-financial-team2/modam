package com.intelliJ_JO.modam.domain.spendrecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsumptionHistoryItemDto {

    private Long id;              // SpendRecord id (null이면 기록 없음)
    private Long transactionId;   // Transaction id
    private String category;
    private String date;          // "2026.05.14"
    private String time;          // "09:20"
    private String place;         // merchantName
    private String memo;
    private String iconName;
    private String emoticon;

    @JsonProperty("isUpdated")
    private boolean isUpdated;

    private boolean hasImage;
    private String imageDesc;
    private boolean hasRecord;
    private int commentCount;
    private Long amount;
}
