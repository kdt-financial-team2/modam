package com.intelliJ_JO.modam.domain.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CardCreateRequestDto {

    @NotNull(message = "연결할 계좌 ID는 필수입니다.")
    private Long accountId;

    @NotNull(message = "발급받는 사용자 ID는 필수입니다.")
    private Long memberId;

    @NotBlank(message = "카드 번호는 필수입니다.")
    private String cardNumber;

    @NotBlank(message = "유효기간은 필수입니다.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "유효기간은 MM/YY 형식이어야 합니다.")
    private String expiryDate;

    // 🔥 [추가] 프론트엔드 워크플로우에서 수집된 추가 데이터 필드
    private String cardDesign;  // 예: pink, mint, yellow, purple
    private String cardType;    // 예: domestic, global
    private String password;    // 4자리 카드 비밀번호
}