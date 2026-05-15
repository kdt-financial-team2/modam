package com.intelliJ_JO.modam.domain.invite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모임 통장 초대 요청 DTO
@Getter
@NoArgsConstructor
public class InviteRequestDto {

    // 초대할 대상 회원 ID
    @NotNull(message = "초대할 회원 ID는 필수입니다.")
    private Long memberId;
}
