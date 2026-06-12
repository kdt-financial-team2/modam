package com.intelliJ_JO.modam.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "월별 소비 추이 응답 DTO")
@Getter
@Builder
public class MonthlyTrendResponseDto {

    @Schema(description = "최근 6개월 월별 소비 합계 목록")
    private List<MonthlyTrendItemDto> trends;
}
