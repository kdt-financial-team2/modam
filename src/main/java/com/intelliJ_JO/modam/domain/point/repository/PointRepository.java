package com.intelliJ_JO.modam.domain.point.repository;

import com.intelliJ_JO.modam.domain.point.entity.PointHistory;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointRepository extends JpaRepository<PointHistory, Long> {

    // =========================================
    // 회원별 포인트 전체 내역 조회
    //
    // ex)
    // 마이페이지 포인트 내역
    // 포인트 사용 내역
    // 포인트 적립 내역
    // =========================================
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // =========================================
    // 회원 최신 포인트 내역 조회
    //
    // 🔥 가장 최근 포인트 잔액 확인용
    //
    // ex)
    // 현재 보유 포인트 조회
    // =========================================
    Optional<PointHistory>
    findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    // =========================================
    // 🔥 잔액 조회 + 비관적 락
    //
    // 동시 적립/사용 요청 시 Race Condition 방지
    // 트랜잭션 종료 전까지 다른 트랜잭션 대기
    // =========================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointHistory p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    List<PointHistory> findLatestByMemberIdWithLock(@Param("memberId") Long memberId, Pageable pageable);

    // =========================================
    // 회원 + 포인트 타입 조회
    //
    // ex)
    // SAVE  → 적립 내역 조회
    // SPEND → 사용 내역 조회
    // =========================================
    List<PointHistory> findByMemberIdAndType(
            Long memberId,
            PointType type
    );

    // =========================================
    // 회원 + 포인트 발생 사유 조회
    //
    // ex)
    // ATTENDANCE
    // CARD_PAYMENT
    // MONTHLY_GOAL
    // =========================================
    List<PointHistory> findByMemberIdAndReason(
            Long memberId,
            PointReason reason
    );

    // =========================================
    // 특정 기간 포인트 내역 조회
    //
    // ex)
    // 월간 리포트
    // 주간 리포트
    // =========================================
    List<PointHistory>
    findByMemberIdAndCreatedAtBetween(
            Long memberId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // =========================================
    // 특정 타입의 최신 포인트 내역 조회
    //
    // ex)
    // 최근 적립 내역
    // 최근 사용 내역
    // =========================================
    Optional<PointHistory>
    findTopByMemberIdAndTypeOrderByCreatedAtDesc(
            Long memberId,
            PointType type
    );

    // =========================================
    // 특정 포인트 지급 여부 확인
    //
    // 🔥 중복 지급 방지용
    //
    // ex)
    // 파트너 초대 보상 1회 지급 검사
    // =========================================
    boolean existsByMemberIdAndReason(
            Long memberId,
            PointReason reason
    );

    // =========================================
    // 🔥 특정 기간 내 포인트 지급 여부 확인
    //
    // ex)
    // 오늘 출석 체크 보상 지급 여부 검사
    //
    // ATTENDANCE + 오늘 날짜 범위 조회용
    // =========================================
    boolean existsByMemberIdAndReasonAndCreatedAtBetween(
            Long memberId,
            PointReason reason,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // =========================================
    // 설명(descrip) 검색
    //
    // ex)
    // "출석"
    // "저축"
    // =========================================
    List<PointHistory>
    findByDescripContaining(String keyword);

    // =========================================
    // 회원별 포인트 전체 내역 페이지 조회 (포인트 상점 내역 탭)
    // =========================================
    Page<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}