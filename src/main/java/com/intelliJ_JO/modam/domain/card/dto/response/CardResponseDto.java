package com.intelliJ_JO.modam.domain.card.dto.response;

import com.intelliJ_JO.modam.domain.card.entity.Card;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CardResponseDto {
    private Long cardId;
    private String cardNumber;  // 카드 번호
    private String expiryDate;  // 유효기간 (MM/YY)
    private String status;      // 상태 (ACTIVE, STOPPED 등)
    private LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 생성자 (서비스 로직에서 아주 편하게 쓰입니다!)
    public CardResponseDto(Card card) {
        this.cardId = card.getId();
        this.cardNumber = card.getCardNumber();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus();
        this.createdAt = card.getCreatedAt();
    }
}