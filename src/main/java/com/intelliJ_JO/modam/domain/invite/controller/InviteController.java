package com.intelliJ_JO.modam.domain.invite.controller;

import com.intelliJ_JO.modam.domain.invite.dto.InviteRequestDto;
import com.intelliJ_JO.modam.domain.invite.dto.InviteResponseDto;
import com.intelliJ_JO.modam.domain.invite.service.InviteService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "초대", description = "모임통장 파트너 초대/수락 API")
@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @Operation(summary = "파트너 초대")
    @PostMapping("/accounts/{accountId}")
    public GlobalResponse<InviteResponseDto> invite(
            @PathVariable Long accountId,
            @Valid @RequestBody InviteRequestDto request) {
        return GlobalResponse.ok(inviteService.invite(accountId, request));
    }

    @Operation(summary = "초대 수락")
    @PatchMapping("/{accountMemberId}/accept")
    public GlobalResponse<InviteResponseDto> accept(
            @PathVariable Long accountMemberId) {
        return GlobalResponse.ok(inviteService.accept(accountMemberId));
    }
}
