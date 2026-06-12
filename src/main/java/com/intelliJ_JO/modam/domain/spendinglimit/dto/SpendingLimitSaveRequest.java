package com.intelliJ_JO.modam.domain.spendinglimit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "소비 제한 저장/수정 요청 DTO")
@Getter
@Setter
public class SpendingLimitSaveRequest {

    @Schema(description = "소비 제한을 적용할 카테고리 목록", example = "[\"식비\", \"쇼핑\"]")
    private List<String> categories;

    @Schema(description = "소비 한도 금액 (원)", example = "200000")
    private Long budgetAmount;

    // =========================
    // 알림 받을 시점
    // =========================
    @Schema(description = "한도 80% 도달 시 알림 여부", example = "true")
    private boolean alertAt80;

    @Schema(description = "한도 100% 도달 시 알림 여부", example = "true")
    private boolean alertAt100;

    // =========================
    // 알림 조건
    // =========================
    @Schema(description = "거래 발생마다 알림 여부", example = "false")
    private boolean everyTransaction;

    @Schema(description = "일일 한도 초과 시 알림 여부", example = "true")
    private boolean dailyLimit;

    @Schema(description = "주간 한도 초과 시 알림 여부", example = "false")
    private boolean weeklyLimit;

    @Schema(description = "고액 거래 발생 시 알림 여부", example = "true")
    private boolean largeAmount;

    // =========================
    // 알림 방식
    // =========================
    @Schema(description = "푸시 알림 여부", example = "true")
    private boolean pushAlert;

    @Schema(description = "이메일 알림 여부", example = "false")
    private boolean emailAlert;

    @Schema(description = "SMS 알림 여부", example = "false")
    private boolean smsAlert;

    @Schema(description = "카카오톡 알림 여부", example = "false")
    private boolean kakaoAlert;
}