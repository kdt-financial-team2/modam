package com.intelliJ_JO.modam.domain.spend.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordResponseDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spend.service.SpendRecordService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spend-records")
@RequiredArgsConstructor
public class SpendRecordController {

    private final SpendRecordService spendRecordService;

    // 소비 기록 생성: 로그인한 본인의 거래에만 생성 가능, 거래 1건당 1개
    @PostMapping
    public GlobalResponse<SpendRecordResponseDto> createSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SpendRecordCreateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.createSpendRecord(userDetails.getMember().getId(), request));
    }

    // 거래 ID로 소비 기록 단건 조회: 해당 계좌 ACCEPT 구성원만 접근 가능
    @GetMapping("/transaction/{transactionId}")
    public GlobalResponse<SpendRecordResponseDto> getSpendRecordByTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long transactionId) {
        return GlobalResponse.ok(spendRecordService.getSpendRecordByTransaction(
                userDetails.getMember().getId(), transactionId));
    }

    // 계좌별 소비 기록 목록: ACCEPT 구성원만 접근, 커서 기반 페이지네이션
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<SpendRecordResponseDto>> getSpendRecordsByAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastRecordId,
            @RequestParam(defaultValue = "10") int size) {
        return GlobalResponse.ok(spendRecordService.getSpendRecordsByAccount(
                userDetails.getMember().getId(), accountId, lastRecordId, size));
    }

    // 소비 기록 수정: null 필드는 기존 값 유지 (PATCH), 본인 거래만 수정 가능
    @PatchMapping("/{recordId}")
    public GlobalResponse<SpendRecordResponseDto> updateSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId,
            @RequestBody SpendRecordUpdateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.updateSpendRecord(
                userDetails.getMember().getId(), recordId, request));
    }

    // 소비 기록 삭제: 본인 거래에 대한 소비 기록만 삭제 가능
    @DeleteMapping("/{recordId}")
    public GlobalResponse<Void> deleteSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId) {
        spendRecordService.deleteSpendRecord(userDetails.getMember().getId(), recordId);
        return GlobalResponse.ok("소비 기록이 삭제되었습니다.");
    }
}
