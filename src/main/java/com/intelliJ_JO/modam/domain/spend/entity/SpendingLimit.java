package com.intelliJ_JO.modam.domain.spend.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spending_limit", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mem_id", "category"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SpendingLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String category;

    @Builder.Default
    @Column(name = "budget_amt", nullable = false)
    private Long budgetAmount = 0L;

    public void updateBudget(Long budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
