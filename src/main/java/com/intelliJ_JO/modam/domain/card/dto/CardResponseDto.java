package com.intelliJ_JO.modam.domain.card.dto;

import com.intelliJ_JO.modam.domain.card.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "카드 응답 DTO")
@Getter
public class CardResponseDto {

    @Schema(description = "카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "카드 번호 (마스킹)", example = "1234-****-****-3456")
    private String cardNumber;

    @Schema(description = "유효기간 (MM/YY 형식)", example = "12/28")
    private String expiryDate;

    @Schema(description = "카드 상태 (ACTIVE / LOST / SUSPENDED)", example = "ACTIVE")
    private String status;

    @Schema(description = "카드 발급일시")
    private LocalDateTime createdAt;

    public CardResponseDto(Card card) {
        this.cardId = card.getId();
        this.cardNumber = card.getCardNumber();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus().name();
        this.createdAt = card.getCreatedAt();
    }
}