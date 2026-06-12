package com.intelliJ_JO.modam.domain.transaction.dto;

import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "거래 생성 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class TransactionRequestDto {

    @Schema(description = "거래 대상 계좌 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    @Schema(description = "거래 요청 사용자 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "카드 ID (카드 결제 시에만 포함, 입출금 시 null)", example = "3")
    private Long cardId;

    @Schema(description = "거래 유형 (DEPOSIT: 입금 / WITHDRAW: 출금 / PAYMENT: 카드결제)", example = "PAYMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "거래 유형(DEPOSIT/WITHDRAW/PAYMENT)은 필수입니다.")
    private TransactionType txType;

    @Schema(description = "거래 금액 (원, 0보다 커야 함)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "거래 금액은 필수입니다.")
    @Positive(message = "거래 금액은 0보다 커야 합니다.")
    private Long amount;

    @Schema(description = "가맹점명 (카드 결제 시)", example = "스타벅스 강남점")
    private String merchantName;

    @Schema(description = "카테고리 (식비, 교통, 데이트 등)", example = "식비")
    private String category;

    @Schema(description = "계좌 비밀번호 (4자리)", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @jakarta.validation.constraints.NotBlank(message = "계좌 비밀번호는 필수입니다.")
    private String accountPassword;
}