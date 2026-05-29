package com.intelliJ_JO.modam.domain.spendinglimit.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitDto;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
import com.intelliJ_JO.modam.domain.spendinglimit.entity.SpendingLimit;
import com.intelliJ_JO.modam.domain.spendinglimit.repository.SpendingLimitRepository;
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
    private static final List<String[]> CATEGORIES = List.of(new String[]{"식비", "utensils"}, new String[]{"교통", "car"}, new String[]{"쇼핑", "shopping-bag"}, new String[]{"의료", "heart-pulse"}, new String[]{"문화/여가", "film"}, new String[]{"기타", "circle-ellipsis"});
    private final SpendingLimitRepository spendingLimitRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final AccountMemberRepository accountMemberRepository;

    // =========================
    // 소비 제한 조회
    // =========================
    public List<SpendingLimitDto> getSpendingLimits(Long memberId) {
        Account account = accountMemberRepository
                .findFirstByMemberId(memberId)
                .map(am -> am.getAccount())
                .orElse(null);

        if (account == null) {
            return CATEGORIES.stream()
                    .map(cat -> new SpendingLimitDto(cat[0], cat[1], 0L, 0L, 0))
                    .collect(Collectors.toList());
        }

        LocalDateTime start =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime end = start.plusMonths(1);

        List<Object[]> rows =
                transactionRepository.sumSpendGroupByCategoryAndMember(
                        memberId,
                        List.of(
                                TransactionType.WITHDRAW,
                                TransactionType.PAYMENT
                        ),
                        start,
                        end
                );

        Map<String, Long> spentMap =
                rows.stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0],
                                r -> ((Number) r[1]).longValue())
                        );

        Map<String, Long> budgetMap =
                spendingLimitRepository.findByAccountId(account.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                SpendingLimit::getCategory,
                                SpendingLimit::getBudgetAmount
                        ));

        return CATEGORIES.stream().map(cat -> {

            String name = cat[0];
            String icon = cat[1];

            long spent =
                    spentMap.getOrDefault(name, 0L);

            long budget =
                    budgetMap.getOrDefault(name, 0L);

            int percentage =
                    budget > 0
                            ? (int) (spent * 100 / budget)
                            : 0;

            return new SpendingLimitDto(
                    name,
                    icon,
                    spent,
                    budget,
                    percentage
            );

        }).collect(Collectors.toList());
    }

    // =========================
    // 소비 제한 저장
    // =========================
    @Transactional
    public String saveSpendingLimits(Long memberId, SpendingLimitSaveRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Account account = accountMemberRepository
                .findFirstByMemberId(memberId)
                .map(am -> am.getAccount())
                .orElseThrow(() -> new IllegalArgumentException("연결된 커플 통장이 없습니다."));

        // 카테고리 미선택 시 전체 카테고리에 적용
        List<String> categories;
        if (request.getCategories() == null || request.getCategories().isEmpty()) {
            categories = CATEGORIES.stream()
                    .map(c -> c[0])
                    .collect(Collectors.toList());
        } else {
            categories = request.getCategories();
        }

        // 이미 존재하는 카테고리가 있으면 EXISTS 반환
        boolean anyExists = categories.stream()
                .anyMatch(cat -> spendingLimitRepository
                        .findByAccountIdAndCategory(account.getId(), cat)
                        .isPresent());

        if (anyExists) {
            return "EXISTS";
        }

        // 카테고리별 개별 레코드 생성
        for (String category : categories) {
            SpendingLimit limit = SpendingLimit.builder()
                    .member(member)
                    .account(account)
                    .category(category)
                    .build();

            limit.updateBudget(request.getBudgetAmount());

            limit.updateSettings(
                    request.isAlertAt80(),
                    request.isAlertAt100(),
                    request.isEveryTransaction(),
                    request.isDailyLimit(),
                    request.isWeeklyLimit(),
                    request.isLargeAmount(),
                    request.isPushAlert(),
                    request.isEmailAlert(),
                    request.isSmsAlert(),
                    request.isKakaoAlert()
            );

            spendingLimitRepository.save(limit);
        }

        return "SUCCESS";
    }

    // =========================
    // 소비 제한 수정
    // =========================
    @Transactional
    public String updateSpendingLimits(
            Long memberId,
            SpendingLimitSaveRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Account account = accountMemberRepository
                .findFirstByMemberId(memberId)
                .map(am -> am.getAccount())
                .orElseThrow(() -> new IllegalArgumentException("연결된 커플 통장이 없습니다."));
        // 카테고리 미선택 시 전체 카테고리에 적용
        List<String> categories;
        if (request.getCategories() == null || request.getCategories().isEmpty()) {
            categories = CATEGORIES.stream()
                    .map(c -> c[0])
                    .collect(Collectors.toList());
        } else {
            categories = request.getCategories();
        }

        // 카테고리별 개별 수정 (없으면 신규 생성)
        for (String category : categories) {
            SpendingLimit limit = spendingLimitRepository
                    .findByAccountIdAndCategory(account.getId(), category)
                    .orElseGet(() -> SpendingLimit.builder()
                            .member(member)
                            .account(account)
                            .category(category)
                            .build());

            limit.updateBudget(request.getBudgetAmount());

            limit.updateSettings(
                    request.isAlertAt80(),
                    request.isAlertAt100(),
                    request.isEveryTransaction(),
                    request.isDailyLimit(),
                    request.isWeeklyLimit(),
                    request.isLargeAmount(),
                    request.isPushAlert(),
                    request.isEmailAlert(),
                    request.isSmsAlert(),
                    request.isKakaoAlert()
            );

            spendingLimitRepository.save(limit);
        }

        return "UPDATED";
    }
}