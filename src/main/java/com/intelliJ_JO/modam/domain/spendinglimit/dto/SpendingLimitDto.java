package com.intelliJ_JO.modam.domain.spendinglimit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "소비 제한 현황 DTO")
@Getter
@AllArgsConstructor
public class SpendingLimitDto {

    @Schema(description = "카테고리명", example = "식비")
    private String category;

    @Schema(description = "카테고리 아이콘명", example = "utensils")
    private String iconName;

    @Schema(description = "현재까지 사용한 금액 (원)", example = "45000")
    private long spent;

    @Schema(description = "설정된 소비 한도 금액 (원)", example = "100000")
    private long budgetAmount;

    @Schema(description = "한도 대비 사용 비율 (%)", example = "45")
    private int percentage;
}
