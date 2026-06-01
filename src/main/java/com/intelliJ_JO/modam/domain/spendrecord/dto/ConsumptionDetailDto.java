package com.intelliJ_JO.modam.domain.spendrecord.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsumptionDetailDto {

    private Long id;             // SpendRecord id
    private Long transactionId;  // Transaction id (수정 페이지 이동에 사용)
    private String title;        // merchantName (상세 제목)
    private String place;        // merchantName
    private String date;      // "2026.05.14"
    private String time;      // "09:20"
    private String category;
    private Long amount;
    private String memo;
    private String imageUrl;
    private String emoticon;
    private String author;    // 작성자 이름
    private int likes;        // 0 (미구현)
}
