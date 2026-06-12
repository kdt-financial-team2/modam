package com.intelliJ_JO.modam.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "AI 소비 인사이트 DTO")
@Getter
@Builder
public class InsightDto {

    @Schema(description = "인사이트 아이콘명", example = "trending-up")
    private String icon;

    @Schema(description = "인사이트 제목", example = "식비 지출 증가")
    private String title;

    @Schema(description = "인사이트 상세 설명", example = "지난달보다 식비가 20% 증가했어요.")
    private String description;
}
