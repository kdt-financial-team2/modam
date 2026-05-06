package com.intelliJ_JO.modam.domain.member.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateRequest {

    private String name;
    private String userId;
    private String pw; // 👉 실제 저장 시 BCrypt로 pwHash 변환 필요
    private String email;
    private boolean agree;
    private boolean notif;
    private String enFirst;
    private String enLast;
    private String bankName;
    private String persAcctNo;
    private String address;
    private String phoneNo;
    private String profileImg;
    private String rrn; // 👉 암호화 필요
}