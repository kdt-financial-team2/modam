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

    // 모임 통장 번호 (acct_id) - FK, 하나의 계좌는 하나의 커플만 소유하므로 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acct_id", nullable = false, unique = true)
    private Account account;

    // 초대 코드 (매칭 및 이메일 초대용)
    @Column(name = "inv_code", nullable = false, length = 50)
    private String inviteCode;

    // 기념일 (메인 대시보드 표시용)
    @Column(name = "d_day")
    private LocalDate dDay;

    // 통장 애칭 (예: 원석과 은아의 여행 자금)
    @Column(name = "acct_alias", length = 100)
    private String accountAlias;

    // 커플 공동 이미지 (통장 메인 배경/프로필용)
    @Column(name = "couple_img", length = 500)
    private String coupleImg;

    // 초대받은 이메일 (대시보드 대기 상태 표시용)
    @Column(name = "inv_email", length = 100)
    private String invitedEmail;

    // 커플 매칭 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 정보 수정 일시
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직 예시: 통장 애칭이나 기념일 변경 메서드
    public void updateCoupleInfo(String accountAlias, LocalDate dDay) {
        this.accountAlias = accountAlias;
        this.dDay = dDay;
    }
}