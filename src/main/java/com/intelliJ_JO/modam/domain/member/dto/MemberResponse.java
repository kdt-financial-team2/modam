package com.intelliJ_JO.modam.domain.member.dto;


import com.intelliJ_JO.modam.domain.member.entity.Member;

import lombok.*;

@Getter @Builder
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String name;
    private String userId;
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