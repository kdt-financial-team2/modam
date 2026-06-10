package com.intelliJ_JO.modam.domain.spendrecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "소비 내역 목록 항목 DTO")
@Getter
@Builder
public class ConsumptionHistoryItemDto {

    @Schema(description = "소비 기록 ID (null이면 기록 없음)", example = "1")
    private Long id;

    @Schema(description = "거래 ID", example = "10")
    private Long transactionId;

    @Schema(description = "카테고리", example = "식비")
    private String category;

    @Schema(description = "거래 날짜 (yyyy.MM.dd 형식)", example = "2026.05.14")
    private String date;

    @Schema(description = "거래 시간 (HH:mm 형식)", example = "09:20")
    private String time;

    @Schema(description = "가맹점명", example = "스타벅스 강남점")
    private String place;

    @Schema(description = "소비 기록 제목", example = "아침 커피")
    private String title;

    @Schema(description = "메모", example = "파트너와 함께")
    private String memo;

    @Schema(description = "카테고리 아이콘명", example = "utensils")
    private String iconName;

    @Schema(description = "이모티콘", example = "☕")
    private String emoticon;

    @JsonProperty("isUpdated")
    @Schema(description = "수정 여부", example = "false")
    private boolean isUpdated;

    @Schema(description = "이미지 첨부 여부", example = "false")
    private boolean hasImage;

    @Schema(description = "이미지 URL", example = "https://example.com/receipt.jpg")
    private String imageUrl;

    @Schema(description = "이미지 설명", example = "영수증 사진")
    private String imageDesc;

    @Schema(description = "소비 기록 작성 여부", example = "true")
    private boolean hasRecord;

    @Schema(description = "댓글 수", example = "3")
    private int commentCount;

    @Schema(description = "거래 금액 (원)", example = "4500")
    private Long amount;

    @Schema(description = "즐겨찾기 여부", example = "false")
    @Builder.Default
    private boolean favorited = false;

    @Schema(description = "소비 기록 작성일시")
    private LocalDateTime createdAt;
}
