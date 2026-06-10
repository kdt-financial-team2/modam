package com.intelliJ_JO.modam.domain.invite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모임 통장 초대 요청 DTO
@Schema(description = "파트너 초대 요청 DTO")
@Getter
@NoArgsConstructor
public class InviteRequestDto {

    @Schema(description = "초대할 대상 회원 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "초대할 회원 ID는 필수입니다.")
    private Long memberId;
}
