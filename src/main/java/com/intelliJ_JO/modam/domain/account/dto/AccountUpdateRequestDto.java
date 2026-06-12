package com.intelliJ_JO.modam.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "계좌 정보 수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AccountUpdateRequestDto {

    @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123")
    private String deliveryAddress;

    @Schema(description = "직업 정보", example = "직장인")
    private String jobInfo;

    @Schema(description = "거래 목적", example = "생활비")
    private String tradePurpose;

    @Schema(description = "자금 출처", example = "근로소득")
    private String fundSource;

    @Schema(description = "소비 한도 금액 (원)", example = "300000")
    private Long spendLimitAmount;

    @Schema(description = "1회 이체 한도 (원)", example = "1000000")
    private Long onceTransferLimit;

    @Schema(description = "일일 이체 한도 (원)", example = "5000000")
    private Long dailyTransferLimit;
}
