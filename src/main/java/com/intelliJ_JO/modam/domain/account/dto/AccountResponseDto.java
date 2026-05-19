package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private String accountType;
    private String status;
    private Long balance;
    private Long availableBalance;
    private Long spendLimitAmount;
    private Long onceTransferLimit;
    private Long dailyTransferLimit;
    private LocalDateTime createdAt;

    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType().name();
        this.status = account.getStatus().name();
        this.balance = account.getBalance();
        this.availableBalance = account.getAvailableBalance();
        this.spendLimitAmount = account.getSpendLimitAmount();
        this.onceTransferLimit = account.getOnceTransferLimit();
        this.dailyTransferLimit = account.getDailyTransferLimit();
        this.createdAt = account.getCreatedAt();
    }
}
