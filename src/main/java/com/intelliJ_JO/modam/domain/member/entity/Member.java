package com.intelliJ_JO.modam.domain.member.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "pw_hash", nullable = false)
    private String pwHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean agree;

    @Column(nullable = false)
    private boolean notif;

    @Column(name = "en_first", nullable = false)
    private String enFirst;

    @Column(name = "en_last", nullable = false)
    private String enLast;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "pers_acct_no", nullable = false)
    private String persAcctNo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.USER;


    @Column(name = "phone_no", nullable = false)
    private String phoneNo;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(nullable = false)
    private String rrn;
}