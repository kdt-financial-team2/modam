package com.intelliJ_JO.modam.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateRequest {

    // 계정 아이디
    @NotBlank
    private String userId;

    // 계정 비밀번호
    @NotBlank
    @Size(min = 8, max = 20)
    private String pw;

    // 비밀번호 확인
    @NotBlank
    private String pwConfirm;

    // 계정 실명
    @NotBlank
    private String name;

    // 영어 이름
    @NotBlank
    private String enLast;

    // 영어 성
    @NotBlank
    private String enFirst;

    // 계정 이메일
    @NotBlank
    @Email
    private String email;

    // 휴대폰 번호
    @NotBlank
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNo;

    // 우편번호
    @NotBlank
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    // 집 주소
    @NotBlank
    private String address;

    // 상세 주소 
    private String addressDetail;

    // 은행명(개인 계좌)
    @NotBlank
    private String bankName;

    // 개인 계좌번호
    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    private String persAcctNo;

    // 주민등록번호
    @NotBlank
    private String rrn;

    // 프로필 이미지
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
