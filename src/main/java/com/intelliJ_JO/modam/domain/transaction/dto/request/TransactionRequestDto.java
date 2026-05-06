package com.intelliJ_JO.modam.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequestDto {

    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;         // 어떤 계좌에서 일어난 거래인지

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long memberId;          // 누가 거래를 일으켰는지

    private Long cardId;            // ✨ 추가: 어떤 카드로 결제했는지 (입출금 시에는 null)

    @NotBlank(message = "거래 유형(DEPOSIT/WITHDRAW/PAYMENT)은 필수입니다.")
    private String transactionType; // "DEPOSIT"(입금), "WITHDRAW"(출금), "PAYMENT"(결제)

    @NotNull(message = "거래 금액은 필수입니다.")
    private Long amount;            // 얼마인지

    private String merchantName;    // 가맹점 이름 (결제일 경우에만 사용, 입출금은 null)

    private String category;        // 카테고리 (식비, 교통비 등)
}