package com.intelliJ_JO.modam.domain.spend.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordResponseDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spend.service.SpendRecordService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소비 기록", description = "소비 기록 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/spend-records")
@RequiredArgsConstructor
public class SpendRecordController {

    private final SpendRecordService spendRecordService;

    @Operation(summary = "소비 기록 생성")
    @PostMapping
    public GlobalResponse<SpendRecordResponseDto> createSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SpendRecordCreateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.createSpendRecord(userDetails.getMember().getId(), request));
    }

    @Operation(summary = "거래별 소비 기록 단건 조회")
    @GetMapping("/transaction/{transactionId}")
    public GlobalResponse<SpendRecordResponseDto> getSpendRecordByTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long transactionId) {
        return GlobalResponse.ok(spendRecordService.getSpendRecordByTransaction(
                userDetails.getMember().getId(), transactionId));
    }

    @Operation(summary = "계좌별 소비 기록 목록 조회 (커서 기반 페이지네이션)")
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<SpendRecordResponseDto>> getSpendRecordsByAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(required = false) Long lastRecordId,
            @RequestParam(defaultValue = "10") int size) {
        return GlobalResponse.ok(spendRecordService.getSpendRecordsByAccount(
                userDetails.getMember().getId(), accountId, lastRecordId, size));
    }

    @Operation(summary = "소비 기록 수정")
    @PatchMapping("/{recordId}")
    public GlobalResponse<SpendRecordResponseDto> updateSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId,
            @RequestBody SpendRecordUpdateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.updateSpendRecord(
                userDetails.getMember().getId(), recordId, request));
    }

    @Operation(summary = "소비 기록 삭제")
    @DeleteMapping("/{recordId}")
    public GlobalResponse<Void> deleteSpendRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId) {
        spendRecordService.deleteSpendRecord(userDetails.getMember().getId(), recordId);
        return GlobalResponse.ok("소비 기록이 삭제되었습니다.");
    }
}
