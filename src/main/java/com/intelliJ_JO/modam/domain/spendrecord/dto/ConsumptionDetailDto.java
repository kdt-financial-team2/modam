package com.intelliJ_JO.modam.domain.spendrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "소비 기록 상세 DTO")
@Getter
@Builder
public class ConsumptionDetailDto {

    @Schema(description = "소비 기록 ID", example = "1")
    private Long id;

    @Schema(description = "거래 ID (수정 페이지 이동 시 사용)", example = "10")
    private Long transactionId;

    @Schema(description = "소비 기록 제목 (가맹점명)", example = "스타벅스 강남점")
    private String title;

    @Schema(description = "가맹점명", example = "스타벅스 강남점")
    private String place;

    @Schema(description = "거래 날짜 (yyyy.MM.dd 형식)", example = "2026.05.14")
    private String date;

    @Schema(description = "거래 시간 (HH:mm 형식)", example = "09:20")
    private String time;

    @Schema(description = "카테고리", example = "식비")
    private String category;

    @Schema(description = "거래 금액 (원)", example = "4500")
    private Long amount;

    @Schema(description = "메모", example = "파트너와 함께한 아침")
    private String memo;

    @Schema(description = "이미지 URL", example = "https://example.com/receipt.jpg")
    private String imageUrl;

    @Schema(description = "이모티콘", example = "☕")
    private String emoticon;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String author;

    @Schema(description = "좋아요 수 (미구현, 항상 0)", example = "0")
    private int likes;
}
