package com.intelliJ_JO.modam.domain.member.dto;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "회원 가입 응답 DTO")
@Getter @Builder
@AllArgsConstructor
public class MemberCreateResponse {

    @Schema(description = "생성된 회원 ID", example = "1")
    private Long id;

    @Schema(description = "로그인 아이디", example = "hong123")
    private String userId;

    public static MemberCreateResponse from(Member m) {
        return MemberCreateResponse.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .build();
    }
}