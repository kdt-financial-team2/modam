package com.intelliJ_JO.modam.domain.account.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "계좌", description = "계좌 개설/조회/수정/해지 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 개설")
    @PostMapping
    public GlobalResponse<AccountResponseDto> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountCreateRequestDto request) {
        return GlobalResponse.ok(accountService.createAccount(request, userDetails.getMember()));
    }

    @Operation(summary = "모임통장 보유 여부 확인")
    @GetMapping("/me/group-status")
    public GlobalResponse<GroupAccountStatusDto> getGroupAccountStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(accountService.getGroupAccountStatus(userDetails.getMember()));
    }

    @Operation(summary = "계좌번호 미리 보기")
    @GetMapping("/preview-number")
    public GlobalResponse<Map<String, String>> previewAccountNumber() {
        return GlobalResponse.ok(Map.of("accountNumber", accountService.generatePreviewAccountNumber()));
    }

    @Operation(summary = "계좌 단건 조회")
    @GetMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> getAccount(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccount(accountId));
    }

    @Operation(summary = "계좌 정보 수정")
    @PatchMapping("/{accountId}")
    public GlobalResponse<AccountResponseDto> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountUpdateRequestDto request) {
        return GlobalResponse.ok(accountService.updateAccount(accountId, request));
    }

    @Operation(summary = "계좌 해지")
    @DeleteMapping("/{accountId}")
    public GlobalResponse<String> closeAccount(@PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return GlobalResponse.ok("계좌가 해지되었습니다.");
    }

    @Operation(summary = "모임통장 참여 회원 목록 조회")
    @GetMapping("/{accountId}/members")
    public GlobalResponse<List<AccountMemberResponseDto>> getAccountMembers(
            @PathVariable Long accountId) {
        return GlobalResponse.ok(accountService.getAccountMembers(accountId));
    }
}
