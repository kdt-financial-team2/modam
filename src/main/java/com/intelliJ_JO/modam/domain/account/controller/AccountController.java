package com.intelliJ_JO.modam.domain.account.controller;

import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberAddRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 개설 (개인/모임 통장)
    @PostMapping
    public GlobalResponse<AccountResponseDto> createAccount(
            @Valid @RequestBody AccountCreateRequestDto request) {
        return GlobalResponse.ok(accountService.createAccount(request));
    }

    // 계좌 단건 조회
    @GetMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> getAccount(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccount(accountId));
    }

    // 계좌 정보 수정
    @PatchMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountUpdateRequestDto request) {
        return GlobalResponse.ok(accountService.updateAccount(accountId, request));
    }

    // 계좌 해지
    @DeleteMapping("/{accountId}")
    public GlobalResponse<String> closeAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return GlobalResponse.ok("계좌가 해지되었습니다.");
    }

    // 모임 통장 참여 회원 목록 조회
    @GetMapping("/{accountId}/members")
    public GlobalResponse<List<AccountMemberResponseDto>> getAccountMembers(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccountMembers(accountId));
    }

    // 모임 통장 파트너 초대
    @PostMapping("/{accountId}/members")
    public GlobalResponse<AccountMemberResponseDto> inviteMember(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountMemberAddRequestDto request) {
        return GlobalResponse.ok(accountService.inviteMember(accountId, request));
    }

    // 초대 수락
    @PatchMapping("/{accountId}/members/{accountMemberId}/accept")
    public GlobalResponse<AccountMemberResponseDto> acceptInvite(
            @PathVariable Long accountId,
            @PathVariable Long accountMemberId) {
        return GlobalResponse.ok(accountService.acceptInvite(accountMemberId));
    }
}
