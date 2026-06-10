package com.intelliJ_JO.modam.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "댓글 작성 요청 DTO")
@Getter
@Setter
public class CommentCreateRequestDto {

    @Schema(description = "댓글 내용 (최대 500자)", example = "오늘 맛있었겠다!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    private String content;

    @Schema(description = "이모티콘", example = "😊")
    private String emoticon;
}
