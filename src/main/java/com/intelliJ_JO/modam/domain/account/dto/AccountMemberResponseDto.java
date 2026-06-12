package com.intelliJ_JO.modam.domain.account.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "모임통장 참여 회원 응답 DTO")
@Getter
public class AccountMemberResponseDto {

    @Schema(description = "AccountMember PK", example = "1")
    private Long id;

    @Schema(description = "회원 ID", example = "2")
    private Long memberId;

    @Schema(description = "회원 이름", example = "홍길동")
    private String memberName;

    @Schema(description = "초대 상태 (WAIT / ACCEPT / REJECT)", example = "ACCEPT")
    private String inviteStatus;

    @Schema(description = "총 입금 누적액 (원)", example = "150000")
    private Long totalDeposit;

    public AccountMemberResponseDto(AccountMember am) {
        this.id = am.getId();
        this.memberId = am.getMember().getId();
        this.memberName = am.getMember().getName();
        this.inviteStatus = am.getInviteStatus().name();
        this.totalDeposit = am.getTotalDeposit();
    }
}
