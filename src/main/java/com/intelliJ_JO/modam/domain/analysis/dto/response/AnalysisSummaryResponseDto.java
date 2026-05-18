package com.intelliJ_JO.modam.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalysisSummaryResponseDto {
    private Long totalSpend;                            // 총 소비
    private Long dailyAvgSpend;                         // 일 평균 소비
    private String topCategory;                         // 가장 많이 사용한 카테고리
    private Long topCategoryAmount;                     // 해당 카테고리 금액
    private Double prevMonthChangeRate;                 // 전월 대비 변화율 (%)
    private String prevMonthChangeDirection;            // "증가" | "감소" | "동일"
    private List<CategoryBreakdownDto> categoryBreakdowns;  // 카테고리별 지출 비율
    private List<InsightDto> insights;                  // AI 소비 인사이트
}
