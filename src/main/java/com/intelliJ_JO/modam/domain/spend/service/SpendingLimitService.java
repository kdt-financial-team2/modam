package com.intelliJ_JO.modam.domain.spend.service;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spend.dto.SpendingLimitDto;
import com.intelliJ_JO.modam.domain.spend.entity.SpendingLimit;
import com.intelliJ_JO.modam.domain.spend.repository.SpendingLimitRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendingLimitService {

    private static final List<String[]> CATEGORIES = List.of(
            new String[]{"식비",    "utensils"},
            new String[]{"교통",    "car"},
            new String[]{"쇼핑",    "shopping-bag"},
            new String[]{"의료",    "heart-pulse"},
            new String[]{"문화/여가", "film"},
            new String[]{"기타",    "circle-ellipsis"}
    );

    private final SpendingLimitRepository spendingLimitRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;

    public List<SpendingLimitDto> getSpendingLimits(Long memberId) {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<Object[]> rows = transactionRepository.sumSpendGroupByCategoryAndMember(
                memberId, List.of(TransactionType.WITHDRAW, TransactionType.PAYMENT), start, end);

        Map<String, Long> spentMap = rows.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));

        Map<String, Long> budgetMap = spendingLimitRepository.findByMemberId(memberId).stream()
                .collect(Collectors.toMap(SpendingLimit::getCategory, SpendingLimit::getBudgetAmount));

        return CATEGORIES.stream().map(cat -> {
            String name = cat[0];
            String icon = cat[1];
            long spent = spentMap.getOrDefault(name, 0L);
            long budget = budgetMap.getOrDefault(name, 0L);
            int percentage = budget > 0 ? (int) (spent * 100 / budget) : 0;
            return new SpendingLimitDto(name, icon, spent, budget, percentage);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void saveSpendingLimits(Long memberId, Map<String, Long> limits) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        limits.forEach((category, budgetAmount) -> {
            SpendingLimit limit = spendingLimitRepository
                    .findByMemberIdAndCategory(memberId, category)
                    .orElse(SpendingLimit.builder().member(member).category(category).build());
            limit.updateBudget(budgetAmount);
            spendingLimitRepository.save(limit);
        });
    }
}
