package com.intelliJ_JO.modam.domain.savings.repository;

import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {

    // 1. 특정 모임 통장에 연결된 모든 저축 목표 목록 조회
    List<Savings> findByAccountId(Long accountId);

    // 2. 특정 모임 통장의 특정 저축 유형(예: 여행, 선물)만 골라서 조회할 때 사용
    List<Savings> findByAccountIdAndSaveType(Long accountId, String saveType);
}