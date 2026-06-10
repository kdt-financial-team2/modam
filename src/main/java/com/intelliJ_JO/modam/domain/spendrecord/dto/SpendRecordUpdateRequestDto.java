package com.intelliJ_JO.modam.domain.spendrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "소비 기록 수정 요청 DTO (null 필드는 기존 값 유지)")
@Getter
@Setter
@NoArgsConstructor
public class SpendRecordUpdateRequestDto {

    @Schema(description = "수정할 이미지 URL", example = "https://example.com/new_receipt.jpg")
    private String imageUrl;

    @Schema(description = "수정할 제목", example = "저녁 식사")
    private String title;

    @Schema(description = "수정할 메모", example = "파트너와 함께한 저녁")
    private String memo;

    @Schema(description = "수정할 이모티콘", example = "🍽")
    private String emoticon;
}
