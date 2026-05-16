package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SavingsResponseDto {
    private Long savingsId;
    private String saveType;       // 저축 유형 (여행, 선물 등)
    private Long targetAmount;     // 목표 금액
    private Long currentAmount;    // 현재 모인 금액
    private LocalDate targetDate;  // D-Day (없을 수도 있음)
    private String isAuto;         // 자동저축 여부 (Y/N)
    private LocalDateTime createdAt;

    // Entity를 받아서 DTO로 변환하는 마법의 생성자
    public SavingsResponseDto(Savings savings) {
        this.savingsId = savings.getId();
        this.saveType = savings.getSaveType();
        this.targetAmount = savings.getTargetAmount();
        this.currentAmount = savings.getCurrentAmount();
        this.targetDate = savings.getTargetDate();
        this.isAuto = savings.getIsAuto();
        this.createdAt = savings.getCreatedAt();
    }
}