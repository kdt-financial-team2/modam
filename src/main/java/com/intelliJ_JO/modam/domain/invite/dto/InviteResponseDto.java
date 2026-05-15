package com.intelliJ_JO.modam.domain.invite.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import lombok.Getter;

// 모임 통장 초대 응답 DTO
@Getter
public class InviteResponseDto {

    private Long id;           // AccountMember PK
    private Long memberId;     // 초대된 회원 ID
    private String memberName; // 초대된 회원 이름
    private String inviteStatus; // 초대 상태 (WAIT / ACCEPT / REJECT)
    private Long totalDeposit;   // 총 입금 누적액

    // AccountMember 엔티티를 응답 DTO로 변환
    public InviteResponseDto(AccountMember accountMember) {
        this.id = accountMember.getId();
        this.memberId = accountMember.getMember().getId();
        this.memberName = accountMember.getMember().getName();
        this.inviteStatus = accountMember.getInviteStatus().name();
        this.totalDeposit = accountMember.getTotalDeposit();
    }
}
