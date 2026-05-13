package com.intelliJ_JO.modam.domain.account.controller;

import com.intelliJ_JO.modam.domain.account.dto.request.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.request.AccountMemberAddRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.request.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.response.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.response.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 개설 (개인/모임 통장)
    @PostMapping
    public ResponseEntity<GlobalResponse<AccountResponseDto>> createAccount(
            @Valid @RequestBody AccountCreateRequestDto request) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.createAccount(request)));
    }

    // 계좌 단건 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<GlobalResponse<AccountResponseDto>> getAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.getAccount(accountId)));
    }

    // 계좌 정보 수정
    @PatchMapping("/{accountId}")
    public ResponseEntity<GlobalResponse<AccountResponseDto>> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountUpdateRequestDto request) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.updateAccount(accountId, request)));
    }

    // 계좌 해지
    @DeleteMapping("/{accountId}")
    public ResponseEntity<GlobalResponse<String>> closeAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok(GlobalResponse.ok("계좌가 해지되었습니다."));
    }

    // 모임 통장 참여 회원 목록
    @GetMapping("/{accountId}/members")
    public ResponseEntity<GlobalResponse<List<AccountMemberResponseDto>>> getAccountMembers(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.getAccountMembers(accountId)));
    }

    // 모임 통장 파트너 초대
    @PostMapping("/{accountId}/members")
    public ResponseEntity<GlobalResponse<AccountMemberResponseDto>> inviteMember(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountMemberAddRequestDto request) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.inviteMember(accountId, request)));
    }

    // 초대 수락
    @PatchMapping("/{accountId}/members/{accountMemberId}/accept")
    public ResponseEntity<GlobalResponse<AccountMemberResponseDto>> acceptInvite(
            @PathVariable Long accountId,
            @PathVariable Long accountMemberId) {
        return ResponseEntity.ok(GlobalResponse.ok(accountService.acceptInvite(accountMemberId)));
    }
}