package com.intelliJ_JO.modam.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "카테고리별 지출 비율 DTO")
@Getter
@Builder
public class CategoryBreakdownDto {

    @Schema(description = "카테고리명", example = "식비")
    private String category;

    @Schema(description = "카테고리 지출 금액 (원)", example = "120000")
    private Long amount;

    @Schema(description = "전체 지출 대비 비율 (%)", example = "34.3")
    private Double percentage;
}
