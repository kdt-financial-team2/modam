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

    // 소비 분석: 기간 내 출금/결제 총합
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.txType IN :types " +
           "AND t.createdAt >= :start AND t.createdAt < :end")
    Long sumSpendByAccountAndPeriod(@Param("accountId") Long accountId,
                                    @Param("types") List<TransactionType> types,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // 소비 분석: 기간 내 카테고리별 합계 (도넛 차트용)
    @Query(value = "SELECT COALESCE(category, '기타'), SUM(amt) " +
                   "FROM transaction WHERE acct_id = :accountId " +
                   "AND tx_type IN ('WITHDRAW', 'PAYMENT') " +
                   "AND created_at >= :start AND created_at < :end " +
                   "GROUP BY COALESCE(category, '기타') ORDER BY SUM(amt) DESC",
           nativeQuery = true)
    List<Object[]> sumSpendGroupByCategory(@Param("accountId") Long accountId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    // 소비 제한: 멤버별 기간 내 카테고리별 지출 합계
    @Query("SELECT COALESCE(t.category, '기타'), SUM(t.amount) FROM Transaction t " +
           "WHERE t.member.id = :memberId AND t.txType IN :types " +
           "AND t.createdAt >= :start AND t.createdAt < :end " +
           "GROUP BY COALESCE(t.category, '기타')")
    List<Object[]> sumSpendGroupByCategoryAndMember(@Param("memberId") Long memberId,
                                                    @Param("types") List<TransactionType> types,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    // 소비 제한 알림용 — 특정 카테고리의 멤버별 월간 지출 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.member.id = :memberId AND t.txType IN :types " +
           "AND COALESCE(t.category, '기타') = :category " +
           "AND t.createdAt >= :start AND t.createdAt < :end")
    Long sumSpendByCategoryAndMember(@Param("memberId") Long memberId,
                                     @Param("types") List<TransactionType> types,
                                     @Param("category") String category,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // 소비 분석: 월별 합계 (라인 차트용) — Oracle 호환 EXTRACT 사용
    @Query(value = "SELECT EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at), SUM(amt) " +
                   "FROM transaction WHERE acct_id = :accountId " +
                   "AND tx_type IN ('WITHDRAW', 'PAYMENT') " +
                   "AND created_at >= :start AND created_at < :end " +
                   "GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at) " +
                   "ORDER BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at)",
           nativeQuery = true)
    List<Object[]> sumSpendGroupByMonth(@Param("accountId") Long accountId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    boolean existsByAccountId(Long accountId);

    // 저축 환급 분배용 — 계좌 내 특정 멤버의 '저축 납입' 트랜잭션 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.member.id = :memberId " +
           "AND t.category = '저축 납입'")
    Long sumSavingsDepositByMember(@Param("accountId") Long accountId,
                                   @Param("memberId") Long memberId);
}
