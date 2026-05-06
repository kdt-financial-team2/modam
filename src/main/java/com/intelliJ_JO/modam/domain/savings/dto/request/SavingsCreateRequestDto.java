package com.intelliJ_JO.modam.domain.savings.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SavingsCreateRequestDto {

    @NotNull(message = "연결할 모임 통장 ID는 필수입니다.")
    private Long accountId;

    @NotBlank(message = "저축 유형(예: 여행, 선물)을 입력해 주세요.")
    private String saveType;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 1000, message = "목표 금액은 최소 1,000원 이상이어야 합니다.") // 💡 최소 금액 방어 로직!
    private Long targetAmount;

    // D-Day는 자유 저축일 경우 없을 수도 있으므로 필수(@NotNull)에서 제외했습니다.
    private LocalDate targetDate;

    // 자동 저축 설정 (프론트에서 값이 안 넘어오면 기본값 'N'으로 처리 예정)
    private String isAuto;

    // 주기 및 금액 (자동 저축일 경우에만 사용)
    private Long autoAmount;
    private String autoCycle; // 매일, 매주, 매월
}