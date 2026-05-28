package com.intelliJ_JO.modam.domain.spendinglimit.entity;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spending_limit", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "category"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SpendingLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 커플 통장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 카테고리
    @Column(nullable = false, length = 50)
    private String category;

    // 소비 한도
    @Builder.Default
    @Column(name = "budget_amt", nullable = false)
    private Long budgetAmount = 0L;

    // =========================
    // 알림 받을 시점
    // =========================

    // 80% 초과 시
    @Builder.Default
    @Column(name = "alert_at_80", nullable = false)
    private boolean alertAt80 = false;

    // 100% 초과 시
    @Builder.Default
    @Column(name = "alert_at_100", nullable = false)
    private boolean alertAt100 = false;

    // =========================
    // 알림 조건
    // =========================

    // 모든 결제 시
    @Builder.Default
    @Column(name = "every_transaction", nullable = false)
    private boolean everyTransaction = false;

    // 일일 한도 초과 시
    @Builder.Default
    @Column(name = "daily_limit", nullable = false)
    private boolean dailyLimit = false;

    // 주간 한도 초과 시
    @Builder.Default
    @Column(name = "weekly_limit", nullable = false)
    private boolean weeklyLimit = false;

    // 고액 결제 시
    @Builder.Default
    @Column(name = "large_amount", nullable = false)
    private boolean largeAmount = false;

    // =========================
    // 알림 방식
    // =========================

    // 푸시 알림
    @Builder.Default
    @Column(name = "push_alert", nullable = false)
    private boolean pushAlert = false;

    // 이메일 알림
    @Builder.Default
    @Column(name = "email_alert", nullable = false)
    private boolean emailAlert = false;

    // 문자 알림
    @Builder.Default
    @Column(name = "sms_alert", nullable = false)
    private boolean smsAlert = false;

    // 카카오톡 알림
    @Builder.Default
    @Column(name = "kakao_alert", nullable = false)
    private boolean kakaoAlert = false;

    // =========================
    // 소비 한도 수정
    // =========================
    public void updateBudget(Long budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    // =========================
    // 알림 설정 수정
    // =========================
    public void updateSettings(

            boolean alertAt80,
            boolean alertAt100,

            boolean everyTransaction,
            boolean dailyLimit,
            boolean weeklyLimit,
            boolean largeAmount,

            boolean pushAlert,
            boolean emailAlert,
            boolean smsAlert,
            boolean kakaoAlert
    ) {

        this.alertAt80 = alertAt80;
        this.alertAt100 = alertAt100;

        this.everyTransaction = everyTransaction;
        this.dailyLimit = dailyLimit;
        this.weeklyLimit = weeklyLimit;
        this.largeAmount = largeAmount;

        this.pushAlert = pushAlert;
        this.emailAlert = emailAlert;
        this.smsAlert = smsAlert;
        this.kakaoAlert = kakaoAlert;
    }
}