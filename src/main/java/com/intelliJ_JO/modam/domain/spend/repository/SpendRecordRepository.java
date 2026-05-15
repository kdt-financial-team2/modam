package com.intelliJ_JO.modam.domain.spend.repository;

import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpendRecordRepository extends JpaRepository<SpendRecord, Long> {

    Optional<SpendRecord> findByTransactionId(Long transactionId);

    boolean existsByTransactionId(Long transactionId);

    // 계좌별 소비 기록 목록 (무한 스크롤 - 처음)
    List<SpendRecord> findByTransaction_AccountIdOrderByIdDesc(Long accountId, Pageable pageable);

    // 계좌별 소비 기록 목록 (무한 스크롤 - 이후)
    List<SpendRecord> findByTransaction_AccountIdAndIdLessThanOrderByIdDesc(Long accountId, Long lastRecordId, Pageable pageable);
}
