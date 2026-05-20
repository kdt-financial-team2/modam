package com.intelliJ_JO.modam.domain.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignupForm implements Serializable {

    // Step 1 - 약관 동의
    private boolean agreeAge;
    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeFinance;
    private boolean agreeMarketing;

    // Step 2 - 개인 정보
    private String userId;
    private String password;
    private String passwordConfirm;
    private String name;
    private String residentNumberFront;
    private String residentNumberBack;
    private String englishLastName;
    private String englishFirstName;
    private String email;
    private String phone;
    private String postalCode;
    private String address;
    private String addressDetail;

    // Step 3 - 계좌 정보
    private String selectedBank;
    private String accountNumber;
}
