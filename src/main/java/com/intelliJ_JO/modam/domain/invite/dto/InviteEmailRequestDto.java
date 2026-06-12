package com.intelliJ_JO.modam.domain.invite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이메일 초대 요청 DTO")
@Getter
@NoArgsConstructor
public class InviteEmailRequestDto {

    @Schema(description = "수신자 이메일 주소", example = "partner@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "수신자 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
