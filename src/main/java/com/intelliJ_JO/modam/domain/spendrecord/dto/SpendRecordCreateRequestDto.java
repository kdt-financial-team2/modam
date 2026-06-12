package com.intelliJ_JO.modam.domain.spendrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "소비 기록 생성 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SpendRecordCreateRequestDto {

    @Schema(description = "연결할 거래 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "거래 ID는 필수입니다.")
    private Long transactionId;

    @Schema(description = "이미지 URL", example = "https://example.com/receipt.jpg")
    private String imageUrl;

    @Schema(description = "소비 기록 제목", example = "점심 식사")
    private String title;

    @Schema(description = "메모", example = "팀원들과 함께한 점심")
    private String memo;

    @Schema(description = "이모티콘", example = "🍜")
    private String emoticon;
}
