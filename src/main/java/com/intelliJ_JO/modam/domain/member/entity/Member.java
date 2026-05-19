package com.intelliJ_JO.modam.domain.member.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    // 필수 약관 동의 4개
    @Column(name = "agree_age", nullable = false)
    private boolean agreeAge;       // 만 14세 이상

    @Column(name = "agree_service", nullable = false)
    private boolean agreeService;   // 모담 이용약관

    @Column(name = "agree_privacy", nullable = false)
    private boolean agreePrivacy;   // 개인정보 수집 및 이용

    @Column(name = "agree_finance", nullable = false)
    private boolean agreeFinance;   // 전자금융거래 이용약관

    // 선택 약관 동의 2개
    @Builder.Default
    @Column(name = "notif", nullable = false)
    private boolean notif = false;          // 마케팅 정보 수신 동의

    @Builder.Default
    @Column(name = "agree_third_party", nullable = false)
    private boolean agreeThirdParty = false; // 개인정보 제3자 제공 동의

    @Column(name = "en_first", nullable = false)
    private String enFirst;

    @Column(name = "en_last", nullable = false)
    private String enLast;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "pers_acct_no", nullable = false)
    private String persAcctNo;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;         // 우편번호

    @Column(nullable = false)
    private String address;         // 기본주소

    @Column(name = "address_detail")
    private String addressDetail;   // 상세주소 (선택)

    @Column(name = "phone_no", nullable = false, unique = true)
    private String phoneNo;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(nullable = false)
    private String rrn;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.USER;

    public void updateInfo(String name, String pwHash, String email,
                           String enFirst, String enLast,
                           String bankName, String persAcctNo,
                           String zipCode, String address, String addressDetail,
                           String phoneNo, String profileImg) {
        if (name != null)          this.name = name;
        if (pwHash != null)        this.pwHash = pwHash;
        if (email != null)         this.email = email;
        if (enFirst != null)       this.enFirst = enFirst;
        if (enLast != null)        this.enLast = enLast;
        if (bankName != null)      this.bankName = bankName;
        if (persAcctNo != null)    this.persAcctNo = persAcctNo;
        if (zipCode != null)       this.zipCode = zipCode;
        if (address != null)       this.address = address;
        if (addressDetail != null) this.addressDetail = addressDetail;
        if (phoneNo != null)       this.phoneNo = phoneNo;
        if (profileImg != null)    this.profileImg = profileImg;
    }

    public void deactivate() {
        this.active = false;
    }
}
