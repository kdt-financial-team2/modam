package com.intelliJ_JO.modam.domain.spendinglimit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpendingLimitDto {
    private String category;
    private String iconName;
    private long spent;
    private long budgetAmount;
    private int percentage;
}
