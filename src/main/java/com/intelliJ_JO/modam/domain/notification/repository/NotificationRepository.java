package com.intelliJ_JO.modam.domain.notification.repository;

import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Notification> findByMemberIdAndIdLessThanOrderByCreatedAtDesc(Long memberId, Long lastId, Pageable pageable);

    List<Notification> findByMemberIdAndIsRead(Long memberId, String isRead);

    long countByMemberIdAndIsRead(Long memberId, String isRead);
}
