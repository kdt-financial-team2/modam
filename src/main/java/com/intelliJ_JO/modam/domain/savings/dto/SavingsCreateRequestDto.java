package com.intelliJ_JO.modam.domain.savings.dto;

import com.intelliJ_JO.modam.domain.savings.entity.AutoCycle;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SavingsCreateRequestDto {

    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    @NotBlank(message = "저축 유형을 입력해주세요. (예: 자유, 여행 등)")
    private String saveType;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 1000, message = "목표 금액은 1000원 이상이어야 합니다.")
    private Long targetAmount;

    @NotNull(message = "목표 날짜는 필수입니다.")
    private LocalDate targetDate;

    private String isAuto; // 자동이체 여부 ('Y' or 'N')

    private Long autoAmount; // ✨ 풀네임으로 수정완료

    // Enum 타입으로 완벽하게 변경된 부분
    private AutoCycle autoCycle;
}