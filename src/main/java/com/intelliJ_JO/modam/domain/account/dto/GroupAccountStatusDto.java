package com.intelliJ_JO.modam.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "모임통장 보유 여부 응답 DTO")
@Getter
@AllArgsConstructor
public class GroupAccountStatusDto {

    @Schema(description = "모임통장 보유 여부", example = "true")
    private boolean hasGroupAccount;

    @Schema(description = "모임통장 ID (없으면 null)", example = "5")
    private Long accountId;

    @Schema(description = "모임통장 계좌번호 (없으면 null)", example = "110-1234-5678")
    private String accountNumber;
}
