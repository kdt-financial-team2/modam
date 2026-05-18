package com.intelliJ_JO.modam.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyTrendItemDto {
    private int year;
    private int month;
    private String label;       // "5월"
    private Long totalAmount;
}
