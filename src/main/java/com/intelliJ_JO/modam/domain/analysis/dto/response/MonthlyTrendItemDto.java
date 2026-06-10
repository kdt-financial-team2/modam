package com.intelliJ_JO.modam.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "월별 소비 추이 항목 DTO")
@Getter
@Builder
public class MonthlyTrendItemDto {

    @Schema(description = "연도", example = "2026")
    private int year;

    @Schema(description = "월", example = "5")
    private int month;

    @Schema(description = "월 레이블", example = "5월")
    private String label;

    @Schema(description = "해당 월 총 소비 금액 (원)", example = "350000")
    private Long totalAmount;
}
