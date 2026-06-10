package com.intelliJ_JO.modam.domain.savings.entity;

import com.intelliJ_JO.modam.domain.account.entity.Account;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false)
    private Account account;

    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;

    @Column(name = "save_type", nullable = false, length = 50)
    private String saveType;

    @Column(name = "target_amt", nullable = false)
    private Long targetAmount;

    @Builder.Default
    @Column(name = "curr_amt", nullable = false)
    private Long currentAmount = 0L;

    @Column(name = "target_dt")
    private LocalDate targetDate;

    @Builder.Default
    @Column(name = "is_auto", nullable = false, length = 1)
    private String isAuto = "N";

    @Column(name = "auto_amt")
    private Long autoAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_cycle", length = 20)
    private AutoCycle autoCycle;

    @Column(name = "my_contribution")
    private Long myContribution;

    @Column(name = "partner_contribution")
    private Long partnerContribution;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive = "Y";

    @Builder.Default
    @Column(name = "is_half_awarded", nullable = false, length = 1)
    private String isHalfAwarded = "N";

    @Builder.Default
    @Column(name = "is_full_awarded", nullable = false, length = 1)
    private String isFullAwarded = "N";

    public void addAmount(Long amount) {
        this.currentAmount += amount;
    }

    public void deactivate() {
        this.isActive = "N";
    }

    public void completeHalfAward() {
        this.isHalfAwarded = "Y";
    }

    public void completeFullAward() {
        this.isFullAwarded = "Y";
    }

    // 🔥 [추가됨] 자동 이체 설정값을 업데이트하는 편의 메서드
    public void updateAutoTransfer(Long autoAmount, AutoCycle autoCycle) {
        this.isAuto = "Y"; // 설정을 저장하면 무조건 자동이체 활성화
        this.autoAmount = autoAmount;
        this.autoCycle = autoCycle;
    }
}