package com.intelliJ_JO.modam.domain.transaction.repository;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // [무한 스크롤용 1] 처음 화면 진입 시 — 가장 최근 ID 순으로 size개 반환
    List<Transaction> findByAccountIdOrderByIdDesc(Long accountId, Pageable pageable);

    // [무한 스크롤용 2] 스크롤 시 — lastTransactionId보다 작은(이전) 거래를 ID 내림차순으로 반환
    List<Transaction> findByAccountIdAndIdLessThanOrderByIdDesc(Long accountId, Long lastTransactionId, Pageable pageable);
}