package com.intelliJ_JO.modam.domain.member.dto;


import com.intelliJ_JO.modam.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "회원 정보 응답 DTO")
@Getter @Builder
@AllArgsConstructor
public class MemberResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "실명", example = "홍길동")
    private String name;

    @Schema(description = "로그인 아이디", example = "hong123")
    private String userId;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    public static MemberResponse from(Member m) {
        return MemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .userId(m.getUserId())
                .email(m.getEmail())
                .build();
    }
}