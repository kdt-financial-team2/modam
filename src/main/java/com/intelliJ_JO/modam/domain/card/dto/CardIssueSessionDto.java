package com.intelliJ_JO.modam.domain.card.dto;

import lombok.Data;

@Data
public class CardIssueSessionDto {
    private Long targetAccountId;    // Step 1: 연결 계좌 ID
    private String cardDesign;       // Step 2: 디자인 (pink, mint 등)
    private String cardType;         // Step 3: 타입 (domestic, global)
    private boolean termsAgreed;     // Step 4: 약관 동의
    private String cardPassword;     // Step 5~6: 비밀번호 4자리
    private String recipientName;    // Step 7: 수령인
    private String shippingAddress;  // Step 7: 배송 주소
    private String contactNumber;    // Step 7: 연락처
}