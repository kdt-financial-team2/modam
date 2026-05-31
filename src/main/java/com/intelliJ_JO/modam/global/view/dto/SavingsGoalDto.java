package com.intelliJ_JO.modam.global.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavingsGoalDto {
    private String goalName;     // 목표명
    private long targetAmount;   // 목표 금액
    private long currentAmount;  // 현재 금액
    private long percent;        // 달성률 (0~100)
}
