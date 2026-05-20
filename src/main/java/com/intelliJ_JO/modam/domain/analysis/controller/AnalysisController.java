package com.intelliJ_JO.modam.domain.analysis.controller;

import com.intelliJ_JO.modam.domain.analysis.dto.response.AnalysisSummaryResponseDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.MonthlyTrendResponseDto;
import com.intelliJ_JO.modam.domain.analysis.service.AnalysisService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "소비 분석", description = "소비 요약/월별 추이 분석 API")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "소비 분석 요약 (카테고리 도넛 차트 + AI 인사이트)")
    @GetMapping("/{accountId}/summary")
    public GlobalResponse<AnalysisSummaryResponseDto> getAnalysisSummary(
            @PathVariable Long accountId,
            @RequestParam int year,
            @RequestParam int month) {
        return GlobalResponse.ok(analysisService.getAnalysisSummary(accountId, year, month));
    }

    @Operation(summary = "월별 소비 추이 조회 (최근 6개월)")
    @GetMapping("/{accountId}/monthly-trend")
    public GlobalResponse<MonthlyTrendResponseDto> getMonthlyTrend(
            @PathVariable Long accountId,
            @RequestParam int year,
            @RequestParam int month) {
        return GlobalResponse.ok(analysisService.getMonthlyTrend(accountId, year, month));
    }
}
