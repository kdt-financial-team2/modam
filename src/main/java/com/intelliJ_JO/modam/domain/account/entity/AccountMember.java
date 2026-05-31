package com.intelliJ_JO.modam.domain.account.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AccountMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계좌 고유번호 (acct_id) - FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false)
    private Account account;

    // 회원 고유번호 (mem_id) - FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 해지 동의 여부 (기본값 'N')
    @Builder.Default
    @Column(name = "agree_cl", nullable = false, length = 1)
    private String agreeClose = "N";

    // 초대 상태 (WAIT, ACCEPT, REJECT / 기본값 'WAIT')
    // 🔥 Enum으로 변경된 코드
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "inv_status", nullable = false, length = 20)
    private InviteStatus inviteStatus = InviteStatus.WAIT;

    // 총 입금 누적액 (비고란 요건 반영: 기본값 0)
    @Builder.Default
    @Column(name = "tot_depo", nullable = false)
    private Long totalDeposit = 0L;

    // 초대 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 초대 수락 및 상태 변경 일시
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직: 초대 수락, 입금 누적액 증가, 해지 동의
    public void acceptInvite() {
        this.inviteStatus = InviteStatus.ACCEPT;
    }

    public void addDeposit(Long amount) {
        this.totalDeposit += amount;
    }

    // 🔥 [수정] 해지 요청 및 취소를 위해 상태를 동적으로 받도록 변경
    public void updateAgreeClose(String agree) {
        this.agreeClose = agree;
    }
}