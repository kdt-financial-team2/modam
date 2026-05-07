package com.intelliJ_JO.modam.domain.notification.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수신자 고유번호 (mem_id) - FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 알림 종류 (DEPOSIT, LIMIT 등)
    @Column(name = "noti_type", nullable = false, length = 20)
    private String notiType;

    // 알림 내용
    @Column(length = 500, nullable = false)
    private String message;

    // 이동 경로(URL) - NULL 허용 (알림 클릭 시 라우팅될 주소)
    @Column(name = "target_url", length = 255)
    private String targetUrl;

    // 읽음 여부 (기본값 'N')
    @Builder.Default
    @Column(name = "is_read", nullable = false, length = 1)
    private String isRead = "N";

    // 발송 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 읽은 일시 (updated_at을 읽은 시간 추적용으로 사용)
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직: 알림 읽음 처리
    public void markAsRead() {
        this.isRead = "Y";
    }
}