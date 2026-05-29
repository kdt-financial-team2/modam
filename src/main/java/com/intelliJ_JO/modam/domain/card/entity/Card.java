package com.intelliJ_JO.modam.domain.card.entity;

import com.intelliJ_JO.modam.domain.account.entity.Account;
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

    // 🔥 [추가] 카드 디자인 저장용 컬럼
    @Column(name = "card_design", length = 20)
    private String cardDesign;

    // 🔥 [추가] 카드 타입 저장용 컬럼 (국내/해외)
    @Column(name = "card_type", length = 20)
    private String cardType;

    // 🔥 [추가] 암호화된 비밀번호 저장용 컬럼
    @Column(name = "card_password", length = 255)
    private String password;

    // 카드 상태 (ACTIVE, LOST, STOPPED)
    // 🔥 Enum으로 변경된 코드
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateStatus(CardStatus status) {
        this.status = status;
    }
}