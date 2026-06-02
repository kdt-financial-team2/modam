package com.intelliJ_JO.modam.domain.spendinglimit.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendingLimitService {
    private static final List<String[]> CATEGORIES = List.of(new String[]{"식비", "utensils"}, new String[]{"교통", "car"}, new String[]{"쇼핑", "shopping-bag"}, new String[]{"의료", "heart-pulse"}, new String[]{"문화/여가", "film"}, new String[]{"기타", "circle-ellipsis"});
    private final SpendingLimitRepository spendingLimitRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;

    // =========================
    // 소비 제한 조회
    // =========================
    public List<SpendingLimitDto> getSpendingLimits(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 회원입니다."
                        ));

        Account account = member.getAccount();

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
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // account 조회
        Account account = member.getAccount();

        // account 연결 안 된 경우
        if (account == null) {
            throw new IllegalArgumentException(
                    "연결된 커플 통장이 없습니다."
            );
        }

        // 선택 카테고리 합치기
        String joinedCategories;

        // 카테고리 선택 안 하면 전체 소비
        if (request.getCategories() == null
                || request.getCategories().isEmpty()) {

            joinedCategories = "전체";

        } else {

            joinedCategories =
                    String.join(",", request.getCategories());
        }
        // 기존 제한 조회
        Optional<SpendingLimit> existingLimit =
                spendingLimitRepository
                        .findByAccountIdAndCategory(
                                account.getId(),
                                joinedCategories
                        );

        // 이미 존재하면 프론트에 상태값 전달
        if (existingLimit.isPresent()) {
            return "EXISTS";
        }

        // 신규 생성
        SpendingLimit limit =
                SpendingLimit.builder()
                        .member(member)
                        .account(account)
                        .category(joinedCategories)
                        .build();

        // 소비 한도 저장
        limit.updateBudget(
                request.getBudgetAmount()
        );

        // 알림 설정 저장
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
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 회원입니다."
                                ));

        Account account = member.getAccount();

        if (account == null) {
            throw new IllegalArgumentException(
                    "연결된 커플 통장이 없습니다."
            );
        }
        // 카테고리 문자열 생성
        String joinedCategories;
        if (request.getCategories() == null
                || request.getCategories().isEmpty()) {
            joinedCategories = "전체";
        } else {
            joinedCategories =
                    String.join(",", request.getCategories());
        }

        // 기존 제한 조회
        SpendingLimit limit =
                spendingLimitRepository
                        .findByAccountIdAndCategory(
                                account.getId(),
                                joinedCategories
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "수정할 소비 제한이 없습니다."
                                ));

        // 소비 한도 수정
        limit.updateBudget(
                request.getBudgetAmount()
        );

        // 알림 설정 수정
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

        return "UPDATED";
    }
}