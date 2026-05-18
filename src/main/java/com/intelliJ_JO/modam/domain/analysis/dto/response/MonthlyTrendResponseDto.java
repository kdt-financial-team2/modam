package com.intelliJ_JO.modam.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MonthlyTrendResponseDto {
    private List<MonthlyTrendItemDto> trends;   // 최근 6개월 월별 소비 합계
}
