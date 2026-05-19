package com.intelliJ_JO.modam.domain.comment.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentUpdateRequestDto {

    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    private String content;     // null이면 기존 값 유지

    private String emoticon;    // null이면 기존 값 유지
}
