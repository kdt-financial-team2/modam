package com.intelliJ_JO.modam.domain.account.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountMemberAddRequestDto {

    @NotNull(message = "초대할 회원 ID는 필수입니다.")
    private Long memberId;
}