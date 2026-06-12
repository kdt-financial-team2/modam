package com.intelliJ_JO.modam.domain.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "카드 발급 단계별 세션 데이터 DTO")
@Data
public class CardIssueSessionDto {

    @Schema(description = "연결할 계좌 ID (Step 1)", example = "1")
    private Long targetAccountId;

    @Schema(description = "카드 디자인 (Step 2, pink / mint / yellow / purple)", example = "pink")
    private String cardDesign;

    @Schema(description = "카드 종류 (Step 3, domestic: 국내전용 / global: 해외겸용)", example = "domestic")
    private String cardType;

    @Schema(description = "약관 동의 여부 (Step 4)", example = "true")
    private boolean termsAgreed;

    @Schema(description = "카드 비밀번호 4자리 (Step 5~6)", example = "1234")
    private String cardPassword;

    @Schema(description = "수령인 이름 (Step 7)", example = "홍길동")
    private String recipientName;

    @Schema(description = "배송 주소 (Step 7)", example = "서울시 강남구 테헤란로 123")
    private String shippingAddress;

    @Schema(description = "연락처 (Step 7)", example = "01012345678")
    private String contactNumber;
}