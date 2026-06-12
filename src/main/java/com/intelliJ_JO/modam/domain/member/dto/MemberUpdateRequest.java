package com.intelliJ_JO.modam.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회원 정보 수정 요청 DTO (null 필드는 기존 값 유지)")
@Getter
@Setter
public class MemberUpdateRequest {

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "변경할 비밀번호", example = "newPassword123!")
    private String password;

    @Schema(description = "변경할 비밀번호 확인", example = "newPassword123!")
    private String passwordConfirm;

    @Schema(description = "이메일 주소", example = "newhong@example.com")
    @Email
    private String email;

    @Schema(description = "영문 이름 (First Name)", example = "Gildong")
    private String enFirst;

    @Schema(description = "영문 성 (Last Name)", example = "Hong")
    private String enLast;

    @Schema(description = "은행명", example = "신한은행")
    private String bankName;

    @Schema(description = "개인 계좌번호 (숫자만)", example = "12345678901234")
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    private String persAcctNo;

    @Schema(description = "우편번호 (5자리 숫자)", example = "12345")
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    @Schema(description = "집 주소", example = "서울시 강남구 테헤란로 456")
    private String address;

    @Schema(description = "상세 주소", example = "202동 303호")
    private String addressDetail;

    @Schema(description = "휴대폰 번호 (01X로 시작하는 10~11자리)", example = "01087654321")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNo;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/new_profile.jpg")
    private String profileImg;
}
