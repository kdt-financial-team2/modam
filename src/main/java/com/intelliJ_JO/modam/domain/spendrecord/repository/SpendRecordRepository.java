package com.intelliJ_JO.modam.domain.spendrecord.repository;

import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpendRecordRepository extends JpaRepository<SpendRecord, Long> {

    Optional<SpendRecord> findByTransactionId(Long transactionId);

    boolean existsByTransactionId(Long transactionId);

    List<SpendRecord> findByTransaction_AccountIdOrderByIdDesc(Long accountId, Pageable pageable);

    List<SpendRecord> findByTransaction_AccountIdAndIdLessThanOrderByIdDesc(Long accountId, Long lastRecordId, Pageable pageable);
}
