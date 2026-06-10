package com.intelliJ_JO.modam.domain.invite.dto;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

// 모임 통장 초대 응답 DTO
@Schema(description = "파트너 초대 응답 DTO")
@Getter
public class InviteResponseDto {

    @Schema(description = "AccountMember PK", example = "1")
    private Long id;

    @Schema(description = "초대된 회원 ID", example = "2")
    private Long memberId;

    @Schema(description = "초대된 회원 이름", example = "홍길동")
    private String memberName;

    @Schema(description = "초대 상태 (WAIT / ACCEPT / REJECT)", example = "WAIT")
    private String inviteStatus;

    @Schema(description = "총 입금 누적액 (원)", example = "0")
    private Long totalDeposit;

    // AccountMember 엔티티를 응답 DTO로 변환
    public InviteResponseDto(AccountMember accountMember) {
        this.id = accountMember.getId();
        this.memberId = accountMember.getMember().getId();
        this.memberName = accountMember.getMember().getName();
        this.inviteStatus = accountMember.getInviteStatus().name();
        this.totalDeposit = accountMember.getTotalDeposit();
    }
}
