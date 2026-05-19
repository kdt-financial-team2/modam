package com.intelliJ_JO.modam.domain.analysis.controller;

import com.intelliJ_JO.modam.domain.analysis.dto.response.AnalysisSummaryResponseDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.MonthlyTrendResponseDto;
import com.intelliJ_JO.modam.domain.analysis.service.AnalysisService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    // GET /api/analysis/{accountId}/summary?year=2026&month=5
    // 4개 요약 카드 + 카테고리 도넛 차트 + AI 인사이트
    @GetMapping("/{accountId}/summary")
    public GlobalResponse<AnalysisSummaryResponseDto> getAnalysisSummary(
            @PathVariable Long accountId,
            @RequestParam int year,
            @RequestParam int month) {
        return GlobalResponse.ok(analysisService.getAnalysisSummary(accountId, year, month));
    }

    // GET /api/analysis/{accountId}/monthly-trend?year=2026&month=5
    // 월별 소비 추이 라인 차트 (해당 월 포함 최근 6개월)
    @GetMapping("/{accountId}/monthly-trend")
    public GlobalResponse<MonthlyTrendResponseDto> getMonthlyTrend(
            @PathVariable Long accountId,
            @RequestParam int year,
            @RequestParam int month) {
        return GlobalResponse.ok(analysisService.getMonthlyTrend(accountId, year, month));
    }
}
