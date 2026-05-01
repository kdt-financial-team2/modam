package com.intelliJ_JO.modam.domain.transaction.repository;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 💡 [무한 스크롤용 1] 처음 화면 진입 시 (가장 최근 내역 10개 호출)
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // 💡 [무한 스크롤용 2] 스크롤을 내렸을 때 (마지막으로 본 ID보다 더 옛날 데이터 10개 호출)
    // "이 계좌의 거래 내역 중, 방금 본 마지막 거래ID(lastTransactionId)보다 작은(이전) 것들을 최신순으로 가져와!"
    List<Transaction> findByAccountIdAndIdLessThanOrderByCreatedAtDesc(Long accountId, Long lastTransactionId, Pageable pageable);
}