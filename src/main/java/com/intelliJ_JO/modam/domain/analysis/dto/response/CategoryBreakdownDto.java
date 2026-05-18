package com.intelliJ_JO.modam.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryBreakdownDto {
    private String category;
    private Long amount;
    private Double percentage;
}
