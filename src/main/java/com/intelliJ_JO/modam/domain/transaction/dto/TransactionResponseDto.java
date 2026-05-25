package com.intelliJ_JO.modam.domain.transaction.dto;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class TransactionResponseDto {

    // 기존 필드
    private Long transactionId;
    private String txType;
    private Long amount;
    private Long afterBalance;
    private String merchantName;
    private String category;

    // 프론트가 필요한 추가 필드
    private String type;      // "deposit" or "withdrawal"
    private String merchant;  // merchantName과 동일
    private Long balance;     // afterBalance와 동일
    private String date;      // "2026.05.07" 형식
    private String time;      // "14:30" 형식
    private String cardName;  // 카드명
    private String iconName;  // 아이콘명

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