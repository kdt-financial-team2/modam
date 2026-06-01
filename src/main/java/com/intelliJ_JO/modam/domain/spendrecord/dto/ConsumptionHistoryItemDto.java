package com.intelliJ_JO.modam.domain.spendrecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConsumptionHistoryItemDto {

    private Long id;              // SpendRecord id (null이면 기록 없음)
    private Long transactionId;   // Transaction id
    private String category;
    private String date;          // "2026.05.14"
    private String time;          // "09:20"
    private String place;         // merchantName
    private String title;         // 소비 기록 시 직접 입력한 제목
    private String memo;
    private String iconName;
    private String emoticon;

    @JsonProperty("isUpdated")
    private boolean isUpdated;

    private boolean hasImage;
    private String imageUrl;
    private String imageDesc;
    private boolean hasRecord;
    private int commentCount;
    private Long amount;

    // 즐겨찾기 여부 (서버에서 주입, 기본값 false)
    @Builder.Default
    private boolean favorited = false;

    // 스토리 작성일 (정렬 기준)
    private LocalDateTime createdAt;
}
