package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SavingsResponseDto {
    private Long savingsId;
    private String goalName;       // 🔥 [추가됨] 목표 이름
    private String saveType;       // 저축 유형 (travel, gift 등)
    private Long targetAmount;
    private Long currentAmount;
    private LocalDate targetDate;
    private String isAuto;
    private LocalDateTime createdAt;
    private int progressPercent;
    private Long myContribution;
    private Long partnerContribution;
    private Long autoAmount;
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