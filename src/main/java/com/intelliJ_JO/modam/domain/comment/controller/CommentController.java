package com.intelliJ_JO.modam.domain.comment.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentCreateRequestDto;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentUpdateRequestDto;
import com.intelliJ_JO.modam.domain.comment.dto.response.CommentResponseDto;
import com.intelliJ_JO.modam.domain.comment.service.CommentService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // POST /api/spend-records/{recordId}/comments — 댓글 작성
    @PostMapping("/api/spend-records/{recordId}/comments")
    public GlobalResponse<CommentResponseDto> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId,
            @Valid @RequestBody CommentCreateRequestDto request) {
        return GlobalResponse.ok(commentService.createComment(
                userDetails.getMember().getId(), recordId, request));
    }

    // GET /api/spend-records/{recordId}/comments — 댓글 목록 조회
    @GetMapping("/api/spend-records/{recordId}/comments")
    public GlobalResponse<List<CommentResponseDto>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId) {
        return GlobalResponse.ok(commentService.getComments(
                userDetails.getMember().getId(), recordId));
    }

    // PATCH /api/comments/{commentId} — 댓글 수정 (본인만)
    @PatchMapping("/api/comments/{commentId}")
    public GlobalResponse<CommentResponseDto> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto request) {
        return GlobalResponse.ok(commentService.updateComment(
                userDetails.getMember().getId(), commentId, request));
    }

    // DELETE /api/comments/{commentId} — 댓글 삭제 (본인만)
    @DeleteMapping("/api/comments/{commentId}")
    public GlobalResponse<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {
        commentService.deleteComment(userDetails.getMember().getId(), commentId);
        return GlobalResponse.ok("댓글이 삭제되었습니다.");
    }
}
