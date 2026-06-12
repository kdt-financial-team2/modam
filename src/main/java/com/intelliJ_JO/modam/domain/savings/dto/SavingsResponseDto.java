package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "저축 목표 응답 DTO")
@Getter
public class SavingsResponseDto {

    @Schema(description = "저축 목표 ID", example = "1")
    private Long savingsId;

    @Schema(description = "목표 이름", example = "제주도 여행")
    private String goalName;

    @Schema(description = "저축 유형 (travel / gift / automatic / free)", example = "travel")
    private String saveType;

    @Schema(description = "목표 금액 (원)", example = "500000")
    private Long targetAmount;

    @Schema(description = "현재 적립 금액 (원)", example = "150000")
    private Long currentAmount;

    @Schema(description = "목표 달성 날짜", example = "2026-12-31")
    private LocalDate targetDate;

    @Schema(description = "자동 납입 여부 (Y / N)", example = "N")
    private String isAuto;

    @Schema(description = "저축 목표 생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "달성률 (%)", example = "30")
    private int progressPercent;

    @Schema(description = "나의 기여금 (원)", example = "75000")
    private Long myContribution;

    @Schema(description = "파트너 기여금 (원)", example = "75000")
    private Long partnerContribution;

    @Schema(description = "자동 납입 금액 (원)", example = "50000")
    private Long autoAmount;

    @Schema(description = "자동 납입 주기 (DAILY / WEEKLY / MONTHLY)", example = "MONTHLY")
    private String autoCycle;

    public SavingsResponseDto(Savings savings) {
        this.savingsId = savings.getId();
        this.goalName = savings.getGoalName();
        this.saveType = savings.getSaveType();
        this.targetAmount = savings.getTargetAmount();
        this.currentAmount = savings.getCurrentAmount();
        this.targetDate = savings.getTargetDate();
        this.isAuto = savings.getIsAuto();
        this.createdAt = savings.getCreatedAt();
        this.myContribution = savings.getMyContribution() != null ? savings.getMyContribution() : 0L;
        this.partnerContribution = savings.getPartnerContribution() != null ? savings.getPartnerContribution() : 0L;
        this.autoAmount = savings.getAutoAmount();
        this.autoCycle  = savings.getAutoCycle() != null ? savings.getAutoCycle().name() : null;

        this.progressPercent = savings.getTargetAmount() > 0
                ? (int) Math.min(((double) savings.getCurrentAmount() / savings.getTargetAmount()) * 100, 100)
                : 0;
    }

    // 🔥 [추가됨] HTML 렌더링을 위한 아이콘 및 한글 라벨 변환 로직
    public String getTypeLabel() {
        return switch (saveType) {
            case "travel" -> "여행";
            case "gift" -> "선물";
            case "automatic" -> "자동저축";
            default -> "자유";
        };
    }

    public String getCycleLabel() {
        if (autoCycle == null) return "매월";
        return switch (autoCycle) {
            case "DAILY"   -> "매일";
            case "WEEKLY"  -> "매주";
            default        -> "매월";
        };
    }

    public String getIconName() {
        return switch (saveType) {
            case "travel" -> "plane";
            case "gift" -> "gift";
            case "automatic" -> "zap";
            default -> "sparkles";
        };
    }
}