package com.intelliJ_JO.modam.domain.member.dto;


import com.intelliJ_JO.modam.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "회원 정보 수정 응답 DTO")
@Getter @Builder
@AllArgsConstructor
public class MemberUpdateResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "수정된 이름", example = "홍길동")
    private String name;

    public static MemberUpdateResponse from(Member m) {
        return MemberUpdateResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .build();
    }
}