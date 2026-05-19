package com.intelliJ_JO.modam.domain.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountUpdateRequestDto {
    private String deliveryAddress;
    private String jobInfo;
    private String tradePurpose;
    private String fundSource;
    private Long spendLimitAmount;
    private Long onceTransferLimit;
    private Long dailyTransferLimit;
}
