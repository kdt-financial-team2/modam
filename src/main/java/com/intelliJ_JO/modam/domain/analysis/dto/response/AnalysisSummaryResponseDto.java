package com.intelliJ_JO.modam.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "소비 분석 요약 응답 DTO")
@Getter
@Builder
public class AnalysisSummaryResponseDto {

    @Schema(description = "총 소비 금액 (원)", example = "350000")
    private Long totalSpend;

    @Schema(description = "일 평균 소비 금액 (원)", example = "11667")
    private Long dailyAvgSpend;

    @Schema(description = "가장 많이 사용한 카테고리", example = "식비")
    private String topCategory;

    @Schema(description = "최다 카테고리 지출 금액 (원)", example = "120000")
    private Long topCategoryAmount;

    @Schema(description = "전월 대비 변화율 (%)", example = "12.5")
    private Double prevMonthChangeRate;

    @Schema(description = "전월 대비 변화 방향 (증가 / 감소 / 동일)", example = "증가")
    private String prevMonthChangeDirection;

    @Schema(description = "카테고리별 지출 비율 목록")
    private List<CategoryBreakdownDto> categoryBreakdowns;

    @Schema(description = "AI 소비 인사이트 목록")
    private List<InsightDto> insights;
}
