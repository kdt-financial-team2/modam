package com.intelliJ_JO.modam.domain.savings.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모임 통장 번호 (acct_id) - FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false)
    private Account account;

    // 저축 유형 (자유, 여행, 선물 등)
    @Column(name = "save_type", nullable = false, length = 50)
    private String saveType;

    // 목표 금액
    @Column(name = "target_amt", nullable = false)
    private Long targetAmount;

    // 현재 모인 금액 (기본값 0)
    @Builder.Default
    @Column(name = "curr_amt", nullable = false)
    private Long currentAmount = 0L;

    // 목표 기한 (D-Day) - 시간은 필요 없으므로 LocalDate 사용
    @Column(name = "target_dt")
    private LocalDate targetDate;

    // 자동저축 여부 (Y/N, 기본값 'N')
    @Builder.Default
    @Column(name = "is_auto", nullable = false, length = 1)
    private String isAuto = "N";

    // 자동이체 금액 (NULL 허용)
    @Column(name = "auto_amt")
    private Long autoAmount;

    // 자동이체 주기 (매일, 매주, 매월 등 - NULL 허용)
    @Column(name = "auto_cycle", length = 20)
    private String autoCycle;

    // 목표 생성 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 목표 수정 일시
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직: 저축 금액 추가 메서드 (나중에 Service에서 사용)
    public void addAmount(Long amount) {
        this.currentAmount += amount;
    }
}