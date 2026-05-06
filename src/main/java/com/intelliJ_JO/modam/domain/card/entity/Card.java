package com.intelliJ_JO.modam.domain.card.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결 계좌 번호 (acct_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false)
    private Account account;

    // 카드 소유자 번호 (mem_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 카드 번호 (UNIQUE, AES-256 암호화를 위해 길이 255)
    @Column(name = "card_no", nullable = false, length = 255, unique = true)
    private String cardNumber;

    // 유효기간 (MM/YY 형식)
    @Column(name = "exp_date", nullable = false, length = 5)
    private String expiryDate;

    // 카드 상태 (ACTIVE, LOST, STOPPED)
    @Column(length = 20, nullable = false)
    private String status;

    // 발급 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 수정 일시 (상태 변경 추적용)
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직: 카드 상태(분실, 정지 등) 변경용 메서드
    public void updateStatus(String status) {
        this.status = status;
    }
}