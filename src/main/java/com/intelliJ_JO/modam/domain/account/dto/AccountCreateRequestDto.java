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

    // 계좌 애칭
    private String acctAlias;

    // 초기 입금액 (선택, 미입력 시 0)
    private Long initialDeposit;

    @NotNull(message = "계좌 비밀번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{4}$", message = "계좌 비밀번호는 숫자 4자리여야 합니다.")
    private String password;

    // 이체 한도
    private Long onceTransferLimit;
    private Long dailyTransferLimit;

    // 고객 정보
    private String jobInfo;
    private String tradePurpose;
    private String fundSource;
    private String deliveryAddress;

    // 공동 계좌 약관 동의 (공동 계좌 개설 시에만 사용)
    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    private boolean agreeService;

    @AssertTrue(message = "전자금융거래 이용약관 동의는 필수입니다.")
    private boolean agreeFinance;

    @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
    private boolean agreePrivacy;

    // 선택 약관 동의
    private boolean agreeMarketing;
}
