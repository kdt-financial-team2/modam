package com.intelliJ_JO.modam.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계좌 번호 (UNIQUE)
    @Column(name = "acct_no", nullable = false, length = 30, unique = true)
    private String accountNumber;

    // 비밀번호 해시 (BCrypt 암호화, NULL 허용)
    @Column(name = "pw_hash", length = 255)
    private String passwordHash;

    // 계좌 상태 (ACTIVE, FREEZE, CLOSED) - 기본값 'ACTIVE'
    // 🔥 Enum으로 변경된 코드
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    // 원장 총 잔액 - 기본값 0
    @Builder.Default
    @Column(name = "bal", nullable = false)
    private Long balance = 0L;

    // 실제 사용가능 금액 (출금 가능 금액) - 기본값 0
    @Builder.Default
    @Column(name = "avail_bal", nullable = false)
    private Long availableBalance = 0L;

    // 소비 제한 한도 (FIN-04 알림용, NULL 허용)
    @Column(name = "limit_amt")
    private Long spendLimitAmount;

    // 배송 주소 (카드 배송지 등, NULL 허용)
    @Column(name = "deliv_addr", length = 255)
    private String deliveryAddress;

    // 직업 정보 (금융법 요건, NULL 허용)
    @Column(name = "job_info", length = 100)
    private String jobInfo;

    // 거래 목적 (금융법 요건, NULL 허용)
    @Column(name = "trade_purp", length = 100)
    private String tradePurpose;

    // 자금 출처 (금융법 요건, NULL 허용)
    @Column(name = "fund_src", length = 100)
    private String fundSource;

    // 생성 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 수정 일시
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직 예시: 잔액 업데이트 메서드
    public void updateBalance(Long amount) {
        this.balance += amount;
        this.availableBalance += amount;
    }
}