package com.intelliJ_JO.modam.domain.account.dto.request;

import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequestDto {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "계좌 유형(PERSONAL/GROUP)은 필수입니다.")
    private AccountType accountType;

    private String password;
    private String deliveryAddress;
    private String jobInfo;
    private String tradePurpose;
    private String fundSource;
}