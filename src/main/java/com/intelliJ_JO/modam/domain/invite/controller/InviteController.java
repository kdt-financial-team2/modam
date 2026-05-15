package com.intelliJ_JO.modam.domain.invite.controller;

import com.intelliJ_JO.modam.domain.invite.dto.InviteRequestDto;
import com.intelliJ_JO.modam.domain.invite.dto.InviteResponseDto;
import com.intelliJ_JO.modam.domain.invite.service.InviteService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 모임 통장 초대 API 컨트롤러
// 초대 관련 엔드포인트를 Account 도메인에서 분리해 /api/invites 경로로 제공
@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    // POST /api/invites/accounts/{accountId}
    // 모임 통장에 파트너 초대 → inviteStatus: WAIT 상태로 생성
    @PostMapping("/accounts/{accountId}")
    public GlobalResponse<InviteResponseDto> invite(
            @PathVariable Long accountId,
            @Valid @RequestBody InviteRequestDto request) {
        return GlobalResponse.ok(inviteService.invite(accountId, request));
    }

    // PATCH /api/invites/{accountMemberId}/accept
    // 초대 수락 → inviteStatus: WAIT → ACCEPT
    @PatchMapping("/{accountMemberId}/accept")
    public GlobalResponse<InviteResponseDto> accept(
            @PathVariable Long accountMemberId) {
        return GlobalResponse.ok(inviteService.accept(accountMemberId));
    }
}
