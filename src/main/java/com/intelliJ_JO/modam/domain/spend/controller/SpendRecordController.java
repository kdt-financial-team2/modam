package com.intelliJ_JO.modam.domain.spend.controller;

import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordResponseDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spend.service.SpendRecordService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spend-records")
@RequiredArgsConstructor
public class SpendRecordController {

    private final SpendRecordService spendRecordService;

    // 소비 기록 생성: 거래 1건당 1개만 생성 가능
    @PostMapping
    public GlobalResponse<SpendRecordResponseDto> createSpendRecord(
            @Valid @RequestBody SpendRecordCreateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.createSpendRecord(request));
    }

    // 거래 ID로 소비 기록 단건 조회
    @GetMapping("/transaction/{transactionId}")
    public GlobalResponse<SpendRecordResponseDto> getSpendRecordByTransaction(
            @PathVariable Long transactionId) {
        return GlobalResponse.ok(spendRecordService.getSpendRecordByTransaction(transactionId));
    }

    // 소비 기록 수정: null 필드는 기존 값 유지 (PATCH)
    @PatchMapping("/{recordId}")
    public GlobalResponse<SpendRecordResponseDto> updateSpendRecord(
            @PathVariable Long recordId,
            @RequestBody SpendRecordUpdateRequestDto request) {
        return GlobalResponse.ok(spendRecordService.updateSpendRecord(recordId, request));
    }

    // 소비 기록 삭제
    @DeleteMapping("/{recordId}")
    public GlobalResponse<String> deleteSpendRecord(@PathVariable Long recordId) {
        spendRecordService.deleteSpendRecord(recordId);
        return GlobalResponse.ok("소비 기록이 삭제되었습니다.");
    }
}
