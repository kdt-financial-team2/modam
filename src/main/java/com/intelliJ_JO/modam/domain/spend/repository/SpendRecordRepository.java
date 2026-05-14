package com.intelliJ_JO.modam.domain.spend.repository;

import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpendRecordRepository extends JpaRepository<SpendRecord, Long> {

    // 거래 ID로 소비 기록 단건 조회 (거래 1건당 소비 기록 1건)
    Optional<SpendRecord> findByTransactionId(Long transactionId);

    // 동일 거래에 소비 기록이 이미 존재하는지 중복 확인
    boolean existsByTransactionId(Long transactionId);
}
