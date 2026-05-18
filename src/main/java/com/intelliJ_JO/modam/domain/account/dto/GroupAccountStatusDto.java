package com.intelliJ_JO.modam.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupAccountStatusDto {
    private boolean hasGroupAccount;
    private Long accountId;
    private String accountNumber;
}
