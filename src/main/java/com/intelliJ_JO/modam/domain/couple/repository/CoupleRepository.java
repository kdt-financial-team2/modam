package com.intelliJ_JO.modam.domain.couple.repository;

import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByAccountId(Long accountId);
    boolean existsByAccountId(Long accountId);
}
