package com.intelliJ_JO.modam.domain.spendinglimit.repository;

import com.intelliJ_JO.modam.domain.spendinglimit.entity.SpendingLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 소비 제한 Repository * * DB의 spending_limit 테이블 접근 담당
 */
public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {
    /**
     * 특정 회원의 소비 제한 목록 조회 * * 예: * 회원이 만든 소비 제한 설정 전체 조회 * * @param memberId 회원 ID * @return 소비 제한 리스트
     */
    List<SpendingLimit> findByMemberId(Long memberId);
}