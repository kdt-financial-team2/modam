package com.intelliJ_JO.modam.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "회원 가입 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateRequest {

    @Schema(description = "로그인 아이디", example = "hong123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String userId;

    @Schema(description = "비밀번호 (8~20자)", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 8, max = 20)
    private String pw;

    @Schema(description = "비밀번호 확인", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String pwConfirm;

    @Schema(description = "실명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "영문 성 (Last Name)", example = "Hong", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String enLast;

    @Schema(description = "영문 이름 (First Name)", example = "Gildong", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String enFirst;

    @Schema(description = "이메일 주소", example = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;

    @Schema(description = "휴대폰 번호 (01X로 시작하는 10~11자리)", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNo;

    @Schema(description = "우편번호 (5자리 숫자)", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    @Schema(description = "집 주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String address;

    @Schema(description = "상세 주소", example = "101동 202호")
    private String addressDetail;

    @Schema(description = "은행명", example = "국민은행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String bankName;

    @Schema(description = "개인 계좌번호 (숫자만)", example = "12345678901234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    private String persAcctNo;

    @Schema(description = "주민등록번호 (13자리)", example = "9001011234567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String rrn;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImg;

    @Schema(description = "만 14세 이상 동의 (필수)", example = "true")
    @AssertTrue(message = "만 14세 이상 동의는 필수입니다.")
    private boolean agreeAge;

    @Schema(description = "모담 이용약관 동의 (필수)", example = "true")
    @AssertTrue(message = "모담 이용약관 동의는 필수입니다.")
    private boolean agreeService;

    @Schema(description = "개인정보 수집 및 이용 동의 (필수)", example = "true")
    @AssertTrue(message = "개인정보 수집 및 이용 동의는 필수입니다.")
    private boolean agreePrivacy;

    @Schema(description = "전자금융거래 이용약관 동의 (필수)", example = "true")
    @AssertTrue(message = "전자금융거래 이용약관 동의는 필수입니다.")
    private boolean agreeFinance;

    @Schema(description = "알림 수신 동의 (선택)", example = "false")
    private boolean notif;

    @Schema(description = "제3자 제공 동의 (선택)", example = "false")
    private boolean agreeThirdParty;
}
