package com.intelliJ_JO.modam.domain.spend.repository;

import com.intelliJ_JO.modam.domain.spend.entity.SpendingLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {
    List<SpendingLimit> findByMemberId(Long memberId);
    Optional<SpendingLimit> findByMemberIdAndCategory(Long memberId, String category);
}
