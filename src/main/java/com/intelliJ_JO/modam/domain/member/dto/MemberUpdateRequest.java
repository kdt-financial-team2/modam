package com.intelliJ_JO.modam.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateRequest {
    private String name;
    private String password;
    private String passwordConfirm;

    @Email
    private String email;

    private String enFirst;
    private String enLast;
    private String bankName;

    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    private String persAcctNo;

    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    private String address;
    private String addressDetail;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNo;

    private String profileImg;
}
