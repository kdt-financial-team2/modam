package com.intelliJ_JO.modam.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "모임통장 회원 추가 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AccountMemberAddRequestDto {

    @Schema(description = "초대할 회원 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "초대할 회원 ID는 필수입니다.")
    private Long memberId;
}
