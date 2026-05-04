package com.intelliJ_JO.modam.domain.transaction.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // ✨ Builder를 쓰기 위한 짝꿍
@Builder // ✨ 서비스 클래스에서 객체를 쉽게 만들게 해줌
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id") // 카드 결제가 아닌 송금일 수 있으므로 nullable
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    @Column(name = "tx_type", nullable = false, length = 20)
    private String transactionType; // DEPOSIT, WITHDRAW, PAYMENT

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "aft_bal", nullable = false)
    private Long afterBalance;

    @Column(name = "merch_nm", length = 100)
    private String merchantName;

    @CreationTimestamp // ✨ INSERT 될 때 자동으로 현재 시간 세팅
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
