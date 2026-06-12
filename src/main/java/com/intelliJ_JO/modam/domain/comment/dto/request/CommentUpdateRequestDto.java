package com.intelliJ_JO.modam.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Schema(description = "댓글 수정 요청 DTO")
@Getter
public class CommentUpdateRequestDto {

    @Schema(description = "수정할 댓글 내용 (null이면 기존 값 유지, 최대 500자)", example = "수정된 댓글입니다.")
    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    private String content;

    @Schema(description = "수정할 이모티콘 (null이면 기존 값 유지)", example = "😄")
    private String emoticon;
}
