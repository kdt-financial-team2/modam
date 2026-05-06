package com.intelliJ_JO.modam.domain.member.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity // JPA 엔티티 선언
@Table(name = "member") // 테이블 이름 지정
@Getter // getter 자동 생성
@NoArgsConstructor // 기본 생성자 생성
@AllArgsConstructor // 전체 생성자 생성
@Builder // 빌더 패턴 적용
public class Member {

    @Id // PK 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id; // 회원 고유 ID

    @Column(nullable = false)
    private String name; // 사용자 이름

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // 로그인 ID (유니크)

    @Column(name = "pw_hash", nullable = false)
    private String pwHash; // 비밀번호 해시값

    @Column(nullable = false, unique = true)
    private String email; // 이메일 (유니크)

    @Column(nullable = false)
    private boolean agree; // 약관 동의 여부

    @Column(nullable = false)
    private boolean notif; // 알림 수신 여부

    @Column(name = "en_first", nullable = false)
    private String enFirst; // 영문 이름 (이름)

    @Column(name = "en_last", nullable = false)
    private String enLast; // 영문 이름 (성)

    @Column(name = "bank_name", nullable = false)
    private String bankName; // 은행명

    @Column(name = "pers_acct_no", nullable = false)
    private String persAcctNo; // 개인 계좌 번호

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 생성일

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true; // 계정 활성화 여부

    @Column(nullable = false, length = 255)
    private String address; // 주소

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.USER; // 권한 (기본 USER)

    @Column(name = "phone_no", nullable = false, unique = true)
    private String phoneNo; // 전화번호

    @Column(name = "profile_img")
    private String profileImg; // 프로필 이미지 URL

    @Column(nullable = false)
    private String rrn; // → encryptedRrn 으로 변경 + 암호화 처리
    // 주민등록번호


}