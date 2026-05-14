package com.intelliJ_JO.modam.domain.point.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity

// =========================================
// 🔥 테이블명
// point 테이블과 매핑
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

    // =========================================
    // 회원 (N:1)
    // 한 명의 회원은 여러 포인트 내역 보유 가능
    // =========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

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
    @Column(name = "type", nullable = false, length = 20)
    private PointType type;

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

    // =========================================
    // 포인트 적립 메서드
    //
    // ex)
    // 출석 보상
    // 저축 목표 달성 보상
    // 소비 절약 보상
    // =========================================
    public void earnPoint(int point) {

        // 🔥 적립 타입 저장
        this.type = PointType.SAVE;

        // 🔥 적립 포인트 저장
        this.amt = point;
    }

    // =========================================
    // 포인트 사용 메서드
    //
    // ex)
    // 상점 아이템 구매
    // 테마 구매
    // 이모티콘 구매
    // =========================================
    public void usePoint(int point) {

        // 🔥 사용 타입 저장
        this.type = PointType.SPEND;

        // 🔥 사용은 음수 처리
        this.amt = -point;
    }

    // =========================================
    // 🔥 저축 목표 달성 포인트 지급 예시
    //
    // Savings 테이블에서:
    // curr_amt >= target_amt
    // 조건 만족 시 포인트 지급 가능
    //
    // ex)
    //
    // if (savings.getCurrAmt() >= savings.getTargetAmt()) {
    //
    //     Point point = Point.builder()
    //             .member(member)
    //             .type(PointType.SAVE)
    //             .amt(3000)
    //             .aftBal(15000)
    //             .descrip("저축 목표 달성 보상")
    //             .build();
    //
    //     pointRepository.save(point);
    // }
    //
    // 의미:
    // +3000 포인트 지급 후
    // 최종 포인트 잔액이 15000 상태 저장
    // =========================================
}
