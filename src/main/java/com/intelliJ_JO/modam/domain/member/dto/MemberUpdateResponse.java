package com.intelliJ_JO.modam.domain.member.dto;


import com.intelliJ_JO.modam.domain.member.entity.Member;

import lombok.*;

@Getter @Builder
@AllArgsConstructor
public class MemberUpdateResponse {
    private Long id;
    private String name;

    public static MemberUpdateResponse from(Member m) {
        return MemberUpdateResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .build();
    }
}