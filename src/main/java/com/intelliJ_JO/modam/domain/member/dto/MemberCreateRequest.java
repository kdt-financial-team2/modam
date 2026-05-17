package com.intelliJ_JO.modam.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateRequest {

    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 8, max = 20)
    private String pw;

    @NotBlank
    private String pwConfirm;

    @NotBlank
    private String name;

    @NotBlank
    private String enLast;

    @NotBlank
    private String enFirst;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNo;

    @NotBlank
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    @NotBlank
    private String address;

    private String addressDetail;

    @NotBlank
    private String bankName;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    private String persAcctNo;

    @NotBlank
    private String rrn;

    private String profileImg;

    // 필수 약관 동의 4개
    @AssertTrue(message = "만 14세 이상 동의는 필수입니다.")
    private boolean agreeAge;

    @AssertTrue(message = "모담 이용약관 동의는 필수입니다.")
    private boolean agreeService;

    @AssertTrue(message = "개인정보 수집 및 이용 동의는 필수입니다.")
    private boolean agreePrivacy;

    @AssertTrue(message = "전자금융거래 이용약관 동의는 필수입니다.")
    private boolean agreeFinance;

    // 선택 약관 동의 2개
    private boolean notif;
    private boolean agreeThirdParty;
}
