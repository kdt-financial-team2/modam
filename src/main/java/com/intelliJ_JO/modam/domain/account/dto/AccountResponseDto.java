package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "계좌 응답 DTO")
@Getter
public class AccountResponseDto {

    @Schema(description = "계좌 ID", example = "1")
    private Long id;

    @Schema(description = "계좌 번호", example = "110-1234-5678")
    private String accountNumber;

    @Schema(description = "계좌 유형 (PERSONAL / GROUP)", example = "PERSONAL")
    private String accountType;

    @Schema(description = "계좌 상태 (ACTIVE / CLOSED)", example = "ACTIVE")
    private String status;

    @Schema(description = "잔액 (원)", example = "500000")
    private Long balance;

    @Schema(description = "출금 가능 잔액 (원)", example = "480000")
    private Long availableBalance;

    @Schema(description = "소비 한도 금액 (원)", example = "300000")
    private Long spendLimitAmount;

    @Schema(description = "1회 이체 한도 (원)", example = "1000000")
    private Long onceTransferLimit;

    @Schema(description = "일일 이체 한도 (원)", example = "5000000")
    private Long dailyTransferLimit;

    @Schema(description = "계좌 개설일시")
    private LocalDateTime createdAt;

    @Schema(description = "계좌 애칭", example = "우리의 여행 통장")
    private String acctAlias;

    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.acctAlias = account.getAcctAlias();
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
