package com.intelliJ_JO.modam.domain.spendinglimit.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SpendingLimitSaveRequest {
    // 선택 카테고리
    private List<String> categories;
    // 소비 한도
    private Long budgetAmount;
    // =========================
    // 알림 받을 시점
    // =========================
    private boolean alertAt80;
    private boolean alertAt100;
    // =========================
    // 알림 조건
    // =========================
    private boolean everyTransaction;
    private boolean dailyLimit;
    private boolean weeklyLimit;
    private boolean largeAmount;
    // =========================
    // 알림 방식
    // =========================
    private boolean pushAlert;
    private boolean emailAlert;
    private boolean smsAlert;
    private boolean kakaoAlert;
}