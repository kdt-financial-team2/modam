package com.intelliJ_JO.modam.domain.spendinglimit.repository;

import com.intelliJ_JO.modam.domain.spendinglimit.entity.SpendingLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 소비 제한 Repository * * spending_limit 테이블 접근 담당
 */
public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {
    /**
     * 특정 커플 통장의 소비 제한 목록 조회 * * @param accountId 커플 통장 ID * @return 소비 제한 리스트
     */
    List<SpendingLimit> findByAccountId(Long accountId);

    /**
     * 같은 커플 통장 + 같은 카테고리 조합 존재 여부 조회 * * 예: * accountId = 1 * category = "식비,교통" * * @param accountId 커플 통장 ID * @param category 카테고리 조합 * @return 소비 제한 Optional
     */
    Optional<SpendingLimit> findByAccountIdAndCategory(Long accountId, String category);
}