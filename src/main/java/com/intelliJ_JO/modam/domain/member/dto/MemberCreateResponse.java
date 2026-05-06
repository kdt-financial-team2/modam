package com.intelliJ_JO.modam.domain.member.dto;

import com.intelliJ_JO.modam.domain.member.entity.Member;

import lombok.*;

@Getter @Builder
@AllArgsConstructor
public class MemberCreateResponse {
    private Long id;
    private String userId;

    public static MemberCreateResponse from(Member m) {
        return MemberCreateResponse.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .build();
    }
}