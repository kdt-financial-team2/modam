package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.AutoCycle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "저축 목표 생성 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SavingsCreateRequestDto {

    @Schema(description = "연결 계좌 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    @Schema(description = "목표 생성 회원 ID (선택)", example = "2")
    private Long memberId;

    @Schema(description = "목표 이름", example = "제주도 여행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "목표 이름을 입력해주세요.")
    private String goalName;

    @Schema(description = "저축 유형 (travel / gift / automatic / free)", example = "travel", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "저축 유형을 입력해주세요.")
    private String saveType;

    @Schema(description = "목표 금액 (원, 최소 1000원)", example = "500000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 1000, message = "목표 금액은 1000원 이상이어야 합니다.")
    private Long targetAmount;

    @Schema(description = "목표 달성 날짜 (yyyy-MM-dd)", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "목표 날짜는 필수입니다.")
    private LocalDate targetDate;

    @Schema(description = "자동 납입 여부 (Y / N)", example = "N")
    private String isAuto;

    @Schema(description = "자동 납입 금액 (원)", example = "50000")
    private Long autoAmount;

    @Schema(description = "자동 납입 주기 (DAILY / WEEKLY / MONTHLY)", example = "MONTHLY")
    private AutoCycle autoCycle;

    @Schema(description = "나의 기여금 (원)", example = "250000")
    private Long myContribution;

    @Schema(description = "파트너 기여금 (원)", example = "250000")
    private Long partnerContribution;
}