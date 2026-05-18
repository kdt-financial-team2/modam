package com.intelliJ_JO.modam.domain.transaction.repository;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // [무한 스크롤용 1] 처음 화면 진입 시 — 가장 최근 ID 순으로 size개 반환
    List<Transaction> findByAccountIdOrderByIdDesc(Long accountId, Pageable pageable);

    // [무한 스크롤용 2] 스크롤 시 — lastTransactionId보다 작은(이전) 거래를 ID 내림차순으로 반환
    List<Transaction> findByAccountIdAndIdLessThanOrderByIdDesc(Long accountId, Long lastTransactionId, Pageable pageable);

    // 1일 이체 한도 경고용 — startOfDay 이후 특정 계좌·멤버의 출금/결제 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.member.id = :memberId " +
           "AND t.txType IN :types AND t.createdAt >= :startOfDay")
    Long sumWithdrawAmountSince(@Param("accountId") Long accountId,
                                @Param("memberId") Long memberId,
                                @Param("types") List<TransactionType> types,
                                @Param("startOfDay") LocalDateTime startOfDay);
}