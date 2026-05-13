package com.intelliJ_JO.modam.domain.card.dto.response;

import com.intelliJ_JO.modam.domain.card.entity.Card;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CardResponseDto {
    private Long cardId;
    private String cardNumber;
    private String expiryDate;
    private String status;
    private LocalDateTime createdAt;

    public CardResponseDto(Card card) {
        this.cardId = card.getId();
        this.cardNumber = card.getCardNumber();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus().name();
        this.createdAt = card.getCreatedAt();
    }
}