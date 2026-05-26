package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.AutoCycle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SavingsCreateRequestDto {

    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    // 🔥 [추가됨] 목표 이름 바인딩
    @NotBlank(message = "목표 이름을 입력해주세요.")
    private String goalName;

    @NotBlank(message = "저축 유형을 입력해주세요.")
    private String saveType;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 1000, message = "목표 금액은 1000원 이상이어야 합니다.")
    private Long targetAmount;

    @NotNull(message = "목표 날짜는 필수입니다.")
    private LocalDate targetDate;

    private String isAuto;
    private Long autoAmount;
    private AutoCycle autoCycle;
}