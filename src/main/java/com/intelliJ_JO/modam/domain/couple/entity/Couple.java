package com.intelliJ_JO.modam.domain.couple.entity;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "couple")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "inv_code", nullable = false, length = 50)
    private String inviteCode;

    @Column(name = "d_day")
    private LocalDate dDay;

    @Column(name = "acct_alias", length = 100)
    private String accountAlias;

    @Column(name = "couple_img", length = 500)
    private String coupleImg;

    @Column(name = "inv_email", length = 100)
    private String invitedEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateCoupleInfo(String accountAlias, LocalDate dDay) {
        this.accountAlias = accountAlias;
        this.dDay = dDay;
    }

    // 🔥 [버그 픽스용] 잘못 생성된 초대 코드를 6자리로 덮어씌우기 위한 메서드
    public void updateInviteCode(String newInviteCode) {
        this.inviteCode = newInviteCode;
    }
}