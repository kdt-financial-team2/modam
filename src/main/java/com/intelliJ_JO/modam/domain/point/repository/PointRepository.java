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

    List<PointHistory> findByCoupleIdOrderByCreatedAtDesc(Long coupleId);

    Optional<PointHistory> findTopByCoupleIdOrderByCreatedAtDesc(Long coupleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointHistory p WHERE p.couple.id = :coupleId ORDER BY p.createdAt DESC")
    List<PointHistory> findLatestByCoupleIdWithLock(@Param("coupleId") Long coupleId, Pageable pageable);

    List<PointHistory> findByCoupleIdAndType(Long coupleId, PointType type);

    List<PointHistory> findByCoupleIdAndReason(Long coupleId, PointReason reason);

    List<PointHistory> findByCoupleIdAndCreatedAtBetween(Long coupleId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<PointHistory> findTopByCoupleIdAndTypeOrderByCreatedAtDesc(Long coupleId, PointType type);

    boolean existsByCoupleIdAndReason(Long coupleId, PointReason reason);

    boolean existsByCoupleIdAndReasonAndCreatedAtBetween(Long coupleId, PointReason reason,
                                                          LocalDateTime startDate, LocalDateTime endDate);

    List<PointHistory> findByDescripContaining(String keyword);

    Page<PointHistory> findByCoupleIdOrderByCreatedAtDesc(Long coupleId, Pageable pageable);
}
