package com.intelliJ_JO.modam.domain.account.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // POST /api/accounts
    // 계좌 개설 — 세션의 로그인 사용자를 개설자로 사용
    @PostMapping
    public GlobalResponse<AccountResponseDto> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountCreateRequestDto request) {
        return GlobalResponse.ok(accountService.createAccount(request, userDetails.getMember()));
    }

    // GET /api/accounts/me/group-status
    // 로그인 후 모임통장 보유 여부 확인 — 2번 화면 분기용
    @GetMapping("/me/group-status")
    public GlobalResponse<GroupAccountStatusDto> getGroupAccountStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(accountService.getGroupAccountStatus(userDetails.getMember()));
    }

    // GET /api/accounts/preview-number
    // 4번 화면 진입 시 계좌번호 미리 생성하여 표시 — 실제 저장은 POST /api/accounts 시점
    @GetMapping("/preview-number")
    public GlobalResponse<Map<String, String>> previewAccountNumber() {
        return GlobalResponse.ok(Map.of("accountNumber", accountService.generatePreviewAccountNumber()));
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
    // 모임 통장 참여 회원 목록 조회
    @GetMapping("/{accountId}/members")
    public GlobalResponse<List<AccountMemberResponseDto>> getAccountMembers(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccountMembers(accountId));
    }
}
