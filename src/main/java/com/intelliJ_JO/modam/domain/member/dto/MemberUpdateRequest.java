package com.intelliJ_JO.modam.domain.member.dto;


import lombok.*;

@Getter @Setter
public class MemberUpdateRequest {
    private String name;
    private String password;
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
    private String rrn;
}