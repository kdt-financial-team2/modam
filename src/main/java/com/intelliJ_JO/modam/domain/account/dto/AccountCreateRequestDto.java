package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequestDto {

    @NotNull(message = "계좌 유형(PERSONAL/GROUP)은 필수입니다.")
    private AccountType accountType;

    @NotNull(message = "계좌 비밀번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{4}$", message = "계좌 비밀번호는 숫자 4자리여야 합니다.")
    private String password;

    @NotNull(message = "계좌 비밀번호 확인은 필수입니다.")
    @Pattern(regexp = "^[0-9]{4}$", message = "계좌 비밀번호 확인은 숫자 4자리여야 합니다.")
    private String passwordConfirm;

    // 이체 한도
    private Long onceTransferLimit;
    private Long dailyTransferLimit;

    // 고객 정보
    private String jobInfo;
    private String tradePurpose;
    private String fundSource;
    private String deliveryAddress;

    // 필수 약관 동의
    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    private boolean agreeService;

    @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
    private boolean agreePrivacy;

    // 선택 약관 동의
    private boolean agreeMarketing;
    private boolean agreeThirdParty;
}
