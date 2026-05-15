package com.intelliJ_JO.modam.domain.account.controller;

import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 계좌 CRUD 및 참여 회원 조회 API 컨트롤러
// 초대(invite) 관련 엔드포인트는 InviteController(/api/invites)로 분리
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // POST /api/accounts
    // 계좌 개설 (개인/모임 통장)
    @PostMapping
    public GlobalResponse<AccountResponseDto> createAccount(
            @Valid @RequestBody AccountCreateRequestDto request) {
        return GlobalResponse.ok(accountService.createAccount(request));
    }

    // GET /api/accounts/{accountId}
    // 계좌 단건 조회
    @GetMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> getAccount(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccount(accountId));
    }

    // PATCH /api/accounts/{accountId}
    // 계좌 정보 수정
    @PatchMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountUpdateRequestDto request) {
        return GlobalResponse.ok(accountService.updateAccount(accountId, request));
    }

    // DELETE /api/accounts/{accountId}
    // 계좌 해지 (status → CLOSED)
    @DeleteMapping("/{accountId}")
    public GlobalResponse<String> closeAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return GlobalResponse.ok("계좌가 해지되었습니다.");
    }

    // GET /api/accounts/{accountId}/members
    // 모임 통장 참여 회원 목록 조회 (초대 상태 무관 전체 반환)
    @GetMapping("/{accountId}/members")
    public GlobalResponse<List<AccountMemberResponseDto>> getAccountMembers(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccountMembers(accountId));
    }
}
