package com.intelliJ_JO.modam.domain.transaction.dto;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Schema(description = "거래 응답 DTO")
@Getter
public class TransactionResponseDto {

    @Schema(description = "거래 ID", example = "1")
    private Long transactionId;

    @Schema(description = "거래 유형 (DEPOSIT / WITHDRAW / PAYMENT)", example = "PAYMENT")
    private String txType;

    @Schema(description = "거래 금액 (원)", example = "15000")
    private Long amount;

    @Schema(description = "거래 후 잔액 (원)", example = "485000")
    private Long afterBalance;

    @Schema(description = "가맹점명", example = "스타벅스 강남점")
    private String merchantName;

    @Schema(description = "카테고리", example = "식비")
    private String category;

    @Schema(description = "거래 방향 (deposit: 입금 / withdrawal: 출금)", example = "withdrawal")
    private String type;

    @Schema(description = "가맹점명 (merchantName과 동일)", example = "스타벅스 강남점")
    private String merchant;

    @Schema(description = "거래 후 잔액 (afterBalance와 동일)", example = "485000")
    private Long balance;

    @Schema(description = "거래 날짜 (yyyy.MM.dd 형식)", example = "2026.05.07")
    private String date;

    @Schema(description = "거래 시간 (HH:mm 형식)", example = "14:30")
    private String time;

    @Schema(description = "카드명", example = "모담 체크카드")
    private String cardName;

    @Schema(description = "카테고리 아이콘명", example = "utensils")
    private String iconName;

    public TransactionResponseDto(Transaction transaction) {
        this.transactionId = transaction.getId();
        this.txType        = transaction.getTxType().name();
        this.amount        = transaction.getAmount();
        this.afterBalance  = transaction.getAfterBalance();
        this.merchantName  = transaction.getMerchantName();
        this.category      = transaction.getCategory();

        // 프론트용 변환
        this.type     = (transaction.getTxType() == TransactionType.DEPOSIT)
                ? "deposit" : "withdrawal";
        this.merchant = transaction.getMerchantName();
        this.balance  = transaction.getAfterBalance();
        this.date     = transaction.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        this.time     = transaction.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("HH:mm"));
        this.cardName = (transaction.getCard() != null)
                ? "모담 체크카드"
                : "-";
        this.iconName = resolveIcon(transaction.getCategory());
    }

    private String resolveIcon(String category) {
        if (category == null) return "circle";
        return switch (category) {
            case "식비"   -> "utensils";
            case "데이트" -> "heart";
            case "생활비" -> "home";
            case "교통비" -> "car";
            case "쇼핑"   -> "shopping-bag";
            case "입금"   -> "wallet";
            default       -> "circle";
        };
    }
}