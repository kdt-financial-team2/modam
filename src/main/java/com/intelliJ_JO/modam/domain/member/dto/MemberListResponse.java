package com.intelliJ_JO.modam.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Schema(description = "회원 목록 응답 DTO")
@Getter
@AllArgsConstructor
public class MemberListResponse {

    @Schema(description = "회원 정보 목록")
    private List<MemberResponse> data;
}