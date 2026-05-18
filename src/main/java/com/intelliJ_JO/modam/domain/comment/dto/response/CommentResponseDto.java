package com.intelliJ_JO.modam.domain.comment.dto.response;

import com.intelliJ_JO.modam.domain.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private final Long id;
    private final Long spendRecordId;
    private final Long memberId;
    private final String memberName;
    private final String content;
    private final String emoticon;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.spendRecordId = comment.getSpendRecord().getId();
        this.memberId = comment.getMember().getId();
        this.memberName = comment.getMember().getName();
        this.content = comment.getContent();
        this.emoticon = comment.getEmoticon();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
