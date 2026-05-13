package com.intelliJ_JO.modam.domain.point.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 고유번호 (mem_id) - FK, 다대일(N:1) 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 구분 (EARN, USE)
    // 🔥 Enum으로 변경된 코드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    // 변동 포인트
    @Column(name = "amt", nullable = false)
    private Long amount;

    // 변동 후 잔여 포인트 (현재 보유 포인트 추적용)
    @Column(name = "aft_bal", nullable = false)
    private Long afterBalance;

    // 변동 사유 (출석, 저축 달성 등)
    @Column(name = "descrip", nullable = false, length = 255)
    private String description;

    // 발생 일시 (내역 테이블이므로 수정 일시 updated_at은 제외)
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 💡 참고: History 테이블은 보통 수정(Update)이 발생하지 않으므로 비즈니스 로직 메서드(ex. updateXXX)를 생략합니다.
}