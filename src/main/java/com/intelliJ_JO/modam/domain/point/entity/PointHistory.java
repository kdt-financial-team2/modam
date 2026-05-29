package com.intelliJ_JO.modam.domain.point.entity;

import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity

// =========================================
// 🔥 테이블명
// point_history 테이블과 매핑
// =========================================
@Table(name = "point_history")

@Getter

// =========================================
// 🔥 Setter 제거
// 엔티티 값 무분별 수정 방지
// =========================================
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@AllArgsConstructor
@Builder

// =========================================
// 🔥 JPA Auditing 적용
// created_at 자동 생성
// =========================================
@EntityListeners(AuditingEntityListener.class)

@SequenceGenerator(
        name = "point_seq_generator",

        // =========================================
        // 🔥 point 테이블용 시퀀스
        // =========================================
        sequenceName = "point_seq",

        allocationSize = 1
)
public class PointHistory {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "point_seq_generator"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    // =========================================
    // 포인트 유형
    //
    // SAVE  : 적립
    // SPEND : 사용
    //
    // 🔥 EnumType.STRING 사용
    // DB에 SAVE / SPEND 문자열 저장
    // =========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private PointType type;

    // =========================================
    // 🔥 포인트 발생 사유 추가
    //
    // ex)
    // ATTENDANCE
    // SAVINGS_50
    // CARD_PAYMENT
    //
    // 🔥 왜 포인트가 지급/사용 되었는지 저장
    //
    // type 과의 차이:
    //
    // type   → SAVE / SPEND
    // reason → 왜 SAVE/SPEND 되었는지
    // =========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private PointReason reason;

    // =========================================
    // 포인트 변화량
    //
    // ex)
    // +100 : 적립
    // -500 : 사용
    // =========================================
    @Column(name = "amt", nullable = false)
    private Integer amt;

    // =========================================
    // 포인트 반영 후 잔액
    // =========================================
    @Column(name = "aft_bal", nullable = false)
    private Integer aftBal;

    // =========================================
    // 포인트 내역 설명
    //
    // ex)
    // "출석 체크 보상"
    // "저축 목표 달성 보상"
    // "아이템 구매"
    // =========================================
    @Column(name = "descrip", nullable = false, length = 255)
    private String descrip;

    // =========================================
    // 생성일
    //
    // 🔥 @CreatedDate
    // INSERT 시 현재 시간 자동 저장
    // =========================================
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
