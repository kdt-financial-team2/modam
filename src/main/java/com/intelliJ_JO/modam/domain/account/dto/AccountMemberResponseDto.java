package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import lombok.Getter;

@Getter
public class AccountMemberResponseDto {
    private Long id;
    private Long memberId;
    private String memberName;
    private String inviteStatus;
    private Long totalDeposit;

    public AccountMemberResponseDto(AccountMember am) {
        this.id = am.getId();
        this.memberId = am.getMember().getId();
        this.memberName = am.getMember().getName();
        this.inviteStatus = am.getInviteStatus().name();
        this.totalDeposit = am.getTotalDeposit();
    }
}
