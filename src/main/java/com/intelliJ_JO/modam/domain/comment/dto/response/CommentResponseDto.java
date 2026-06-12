package com.intelliJ_JO.modam.domain.comment.dto.response;

import com.intelliJ_JO.modam.domain.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "댓글 응답 DTO")
@Getter
public class CommentResponseDto {

    @Schema(description = "댓글 ID", example = "1")
    private final Long id;

    @Schema(description = "소비 기록 ID", example = "10")
    private final Long spendRecordId;

    @Schema(description = "작성자 회원 ID", example = "2")
    private final Long memberId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private final String memberName;

    @Schema(description = "댓글 내용", example = "오늘 맛있었겠다!")
    private final String content;

    @Schema(description = "이모티콘", example = "😊")
    private final String emoticon;

    @Schema(description = "댓글 작성일시")
    private final LocalDateTime createdAt;

    @Schema(description = "댓글 수정일시")
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
