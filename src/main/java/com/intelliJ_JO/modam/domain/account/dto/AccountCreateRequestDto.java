package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "계좌 개설 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequestDto {

    @Schema(description = "계좌 유형 (PERSONAL: 개인 계좌, GROUP: 모임 계좌)", example = "PERSONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "계좌 유형(PERSONAL/GROUP)은 필수입니다.")
    private AccountType accountType;

    // 계좌 애칭
    @Schema(description = "계좌 애칭", example = "우리의 여행 통장")
    private String acctAlias;

    // 초기 입금액 (선택, 미입력 시 0)
    @Schema(description = "초기 입금액 (미입력 시 0)", example = "50000")
    private Long initialDeposit;

    @Schema(description = "계좌 비밀번호 (숫자 4자리)", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "계좌 비밀번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{4}$", message = "계좌 비밀번호는 숫자 4자리여야 합니다.")
    private String password;

    @Schema(description = "계좌 비밀번호 확인", example = "1234")
    private String passwordConfirm;

    // 이체 한도
    @Schema(description = "1회 이체 한도 (원)", example = "1000000")
    private Long onceTransferLimit;

    @Schema(description = "일일 이체 한도 (원)", example = "5000000")
    private Long dailyTransferLimit;

    // 고객 정보
    @Schema(description = "직업 정보", example = "직장인")
    private String jobInfo;

    @Schema(description = "거래 목적", example = "생활비")
    private String tradePurpose;

    @Schema(description = "자금 출처", example = "근로소득")
    private String fundSource;

    @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123")
    private String deliveryAddress;

    // 공동 계좌 약관 동의 (공동 계좌 개설 시에만 사용)
    @Schema(description = "서비스 이용약관 동의 (필수)", example = "true")
    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    private boolean agreeService;

    @Schema(description = "전자금융거래 이용약관 동의 (필수)", example = "true")
    @AssertTrue(message = "전자금융거래 이용약관 동의는 필수입니다.")
    private boolean agreeFinance;

    @Schema(description = "개인정보 처리방침 동의 (필수)", example = "true")
    @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
    private boolean agreePrivacy;

    // 선택 약관 동의
    @Schema(description = "마케팅 수신 동의 (선택)", example = "false")
    private boolean agreeMarketing;

    @Schema(description = "제3자 제공 동의 (선택)", example = "false")
    private boolean agreeThirdParty;
}
