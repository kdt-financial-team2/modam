package com.intelliJ_JO.modam.domain.analysis.service;

import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.analysis.dto.response.AnalysisSummaryResponseDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.CategoryBreakdownDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.InsightDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.MonthlyTrendItemDto;
import com.intelliJ_JO.modam.domain.analysis.dto.response.MonthlyTrendResponseDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private static final List<TransactionType> SPEND_TYPES =
            List.of(TransactionType.WITHDRAW, TransactionType.PAYMENT);

    public AnalysisSummaryResponseDto getAnalysisSummary(Long accountId, int year, int month) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다.");
        }

        YearMonth ym = YearMonth.of(year, month);
        YearMonth prevYm = ym.minusMonths(1);

        LocalDateTime curStart = ym.atDay(1).atStartOfDay();
        LocalDateTime curEnd = ym.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime prevStart = prevYm.atDay(1).atStartOfDay();
        LocalDateTime prevEnd = ym.atDay(1).atStartOfDay();

        Long totalSpend = transactionRepository.sumSpendByAccountAndPeriod(
                accountId, SPEND_TYPES, curStart, curEnd);
        Long prevTotalSpend = transactionRepository.sumSpendByAccountAndPeriod(
                accountId, SPEND_TYPES, prevStart, prevEnd);

        long dailyAvg = ym.lengthOfMonth() > 0 ? totalSpend / ym.lengthOfMonth() : 0L;

        List<Object[]> categoryData = transactionRepository.sumSpendGroupByCategory(
                accountId, curStart, curEnd);
        List<Object[]> prevCategoryData = transactionRepository.sumSpendGroupByCategory(
                accountId, prevStart, prevEnd);

        List<CategoryBreakdownDto> breakdowns = buildCategoryBreakdowns(categoryData, totalSpend);
        String topCategory = breakdowns.isEmpty() ? "-" : breakdowns.get(0).getCategory();
        Long topCategoryAmount = breakdowns.isEmpty() ? 0L : breakdowns.get(0).getAmount();

        double changeRate = 0.0;
        String changeDirection = "동일";
        if (prevTotalSpend > 0) {
            changeRate = Math.round(
                    Math.abs((double)(totalSpend - prevTotalSpend) / prevTotalSpend * 100) * 10.0) / 10.0;
            if (totalSpend > prevTotalSpend) changeDirection = "증가";
            else if (totalSpend < prevTotalSpend) changeDirection = "감소";
        }

        Map<String, Long> curMap = toMap(categoryData);
        Map<String, Long> prevMap = toMap(prevCategoryData);
        List<InsightDto> insights = generateInsights(curMap, prevMap, totalSpend);

        return AnalysisSummaryResponseDto.builder()
                .totalSpend(totalSpend)
                .dailyAvgSpend(dailyAvg)
                .topCategory(topCategory)
                .topCategoryAmount(topCategoryAmount)
                .prevMonthChangeRate(changeRate)
                .prevMonthChangeDirection(changeDirection)
                .categoryBreakdowns(breakdowns)
                .insights(insights)
                .build();
    }

    public MonthlyTrendResponseDto getMonthlyTrend(Long accountId, int year, int month) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다.");
        }

        YearMonth targetYm = YearMonth.of(year, month);
        YearMonth startYm = targetYm.minusMonths(5);

        LocalDateTime start = startYm.atDay(1).atStartOfDay();
        LocalDateTime end = targetYm.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> monthlyData = transactionRepository.sumSpendGroupByMonth(accountId, start, end);

        // (year-month) → 합계 맵으로 변환
        Map<String, Long> monthlyMap = new HashMap<>();
        for (Object[] row : monthlyData) {
            String key = ((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue();
            monthlyMap.put(key, ((Number) row[2]).longValue());
        }

        // 6개월 전부 순서대로 채우기 (거래 없는 달은 0)
        List<MonthlyTrendItemDto> trends = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = targetYm.minusMonths(i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            trends.add(MonthlyTrendItemDto.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .label(ym.getMonthValue() + "월")
                    .totalAmount(monthlyMap.getOrDefault(key, 0L))
                    .build());
        }

        return MonthlyTrendResponseDto.builder().trends(trends).build();
    }

    private List<CategoryBreakdownDto> buildCategoryBreakdowns(List<Object[]> data, Long totalSpend) {
        return data.stream()
                .map(row -> {
                    String category = row[0] != null ? (String) row[0] : "기타";
                    Long amount = ((Number) row[1]).longValue();
                    double percentage = totalSpend > 0
                            ? Math.round((double) amount / totalSpend * 1000.0) / 10.0
                            : 0.0;
                    return CategoryBreakdownDto.builder()
                            .category(category)
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> toMap(List<Object[]> data) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : data) {
            String category = row[0] != null ? (String) row[0] : "기타";
            map.put(category, ((Number) row[1]).longValue());
        }
        return map;
    }

    private List<InsightDto> generateInsights(Map<String, Long> current,
                                               Map<String, Long> prev,
                                               Long totalSpend) {
        List<InsightDto> insights = new ArrayList<>();

        // Rule 1: 전월 대비 10% 이상 증가한 카테고리 (증가율 높은 순 최대 2개)
        current.entrySet().stream()
                .filter(e -> {
                    Long prevAmt = prev.getOrDefault(e.getKey(), 0L);
                    return prevAmt > 0 && (double)(e.getValue() - prevAmt) / prevAmt >= 0.10;
                })
                .sorted((a, b) -> {
                    double ra = (double)(a.getValue() - prev.getOrDefault(a.getKey(), 0L))
                                / prev.getOrDefault(a.getKey(), 1L);
                    double rb = (double)(b.getValue() - prev.getOrDefault(b.getKey(), 0L))
                                / prev.getOrDefault(b.getKey(), 1L);
                    return Double.compare(rb, ra);
                })
                .limit(2)
                .forEach(e -> {
                    Long prevAmt = prev.get(e.getKey());
                    double rate = Math.round((double)(e.getValue() - prevAmt) / prevAmt * 1000) / 10.0;
                    insights.add(InsightDto.builder()
                            .icon(getCategoryIcon(e.getKey()))
                            .title(String.format("이번 달 %s 비용이 지난달보다 %.0f%% 증가했어요", e.getKey(), rate))
                            .description(getCategoryChangeComment(e.getKey()))
                            .build());
                });

        // Rule 2: 전체의 35% 이상인 카테고리 (아직 insights에 없는 것만)
        if (totalSpend > 0 && insights.size() < 3) {
            current.entrySet().stream()
                    .filter(e -> (double) e.getValue() / totalSpend >= 0.35)
                    .filter(e -> insights.stream().noneMatch(i -> i.getTitle().contains(e.getKey())))
                    .findFirst()
                    .ifPresent(e -> {
                        double pct = Math.round((double) e.getValue() / totalSpend * 1000) / 10.0;
                        insights.add(InsightDto.builder()
                                .icon(getCategoryIcon(e.getKey()))
                                .title(String.format("%s 소비가 전체의 %.0f%%를 차지하고 있어요", e.getKey(), pct))
                                .description(getCategorySavingsTip(e.getKey()))
                                .build());
                    });
        }

        return insights;
    }

    private String getCategoryIcon(String category) {
        return switch (category) {
            case "식비" -> "🍽";
            case "데이트" -> "❤";
            case "생활비", "상활비" -> "🏠";
            case "교통비" -> "🚗";
            case "쇼핑" -> "🛍";
            case "문화" -> "🎬";
            default -> "💰";
        };
    }

    private String getCategoryChangeComment(String category) {
        return switch (category) {
            case "식비" -> "외식이 많아졌네요! 집밥을 활용하면 절약할 수 있어요.";
            case "데이트" -> "함께하는 시간이 많아졌네요! 특별한 날이 있으셨나요?";
            case "교통비" -> "이동이 많은 달이었네요!";
            case "쇼핑" -> "쇼핑이 늘었네요! 위시리스트를 활용해 충동 구매를 줄여 보세요.";
            default -> "지출이 늘어난 항목이에요. 예산을 확인해 보세요.";
        };
    }

    private String getCategorySavingsTip(String category) {
        return switch (category) {
            case "식비" -> "외식 빈도가 높은 편이에요. 주 1-2회 집밥 요리로 월 10만원 절약 가능해요!";
            case "데이트" -> "데이트 비용이 높아요. 집에서 즐기는 홈데이트도 추천드려요!";
            case "교통비" -> "교통비 지출이 많아요. 대중교통 정기권을 활용해 보세요!";
            case "쇼핑" -> "쇼핑 지출이 많아요. 구매 전 하루 더 생각해 보는 습관이 도움이 돼요!";
            default -> "해당 항목 지출이 높아요. 월별 예산을 설정해 관리해 보세요!";
        };
    }
}
