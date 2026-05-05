package com.intelliJ_JO.modam.domain.member.dto;


import lombok.*;

import java.util.List;


@Getter
@AllArgsConstructor
public class MemberListResponse {
    private List<MemberResponse> data;
}