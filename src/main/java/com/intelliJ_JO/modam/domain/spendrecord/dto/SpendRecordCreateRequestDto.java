package com.intelliJ_JO.modam.domain.spendrecord.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpendRecordCreateRequestDto {

    @NotNull(message = "거래 ID는 필수입니다.")
    private Long transactionId;

    private String imageUrl;
    private String memo;
    private String emoticon;
}
