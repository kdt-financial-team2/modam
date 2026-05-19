package com.intelliJ_JO.modam.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsightDto {
    private String icon;
    private String title;
    private String description;
}
