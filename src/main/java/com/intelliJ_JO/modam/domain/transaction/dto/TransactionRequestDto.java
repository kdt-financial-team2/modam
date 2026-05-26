package com.intelliJ_JO.modam.domain.transaction.dto;

import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequestDto {

    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long memberId;

    private Long cardId; // 카드 결제 시에만 포함, 입출금 시 null

    @NotNull(message = "거래 유형(DEPOSIT/WITHDRAW/PAYMENT)은 필수입니다.")
    private TransactionType txType;

    @NotNull(message = "거래 금액은 필수입니다.")
    @Positive(message = "거래 금액은 0보다 커야 합니다.")
    private Long amount;

    private String merchantName; // 가맹점명 (결제 시)
    private String category;     // 카테고리 (식비, 교통 등)

    // [추가] 계좌 비밀번호
    @jakarta.validation.constraints.NotBlank(message = "계좌 비밀번호는 필수입니다.")
    private String accountPassword;
}