package com.intelliJ_JO.modam.domain.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "카드 발급 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class CardCreateRequestDto {

    @Schema(description = "연결할 계좌 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "연결할 계좌 ID는 필수입니다.")
    private Long accountId;

    @Schema(description = "발급받는 사용자 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "발급받는 사용자 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "카드 번호 (16자리)", example = "1234-5678-9012-3456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "카드 번호는 필수입니다.")
    private String cardNumber;

    @Schema(description = "유효기간 (MM/YY 형식)", example = "12/28", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "유효기간은 필수입니다.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "유효기간은 MM/YY 형식이어야 합니다.")
    private String expiryDate;

    @Schema(description = "카드 디자인 (pink / mint / yellow / purple)", example = "pink")
    private String cardDesign;

    @Schema(description = "카드 종류 (domestic: 국내전용 / global: 해외겸용)", example = "domestic")
    private String cardType;

    @Schema(description = "카드 비밀번호 (숫자 4자리)", example = "1234")
    private String password;
}