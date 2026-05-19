package com.intelliJ_JO.modam.domain.comment.service;

import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentCreateRequestDto;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentUpdateRequestDto;
import com.intelliJ_JO.modam.domain.comment.dto.response.CommentResponseDto;
import com.intelliJ_JO.modam.domain.comment.entity.Comment;
import com.intelliJ_JO.modam.domain.comment.repository.CommentRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spend.repository.SpendRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final SpendRecordRepository spendRecordRepository;
    private final MemberRepository memberRepository;
    private final AccountMemberRepository accountMemberRepository;

    // 댓글 작성 — 해당 계좌의 ACCEPT 구성원만 가능
    @Transactional
    public CommentResponseDto createComment(Long memberId, Long recordId,
                                            CommentCreateRequestDto request) {
        SpendRecord spendRecord = findSpendRecord(recordId);
        validateAcceptMember(spendRecord, memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .spendRecord(spendRecord)
                .member(member)
                .content(request.getContent())
                .emoticon(request.getEmoticon())
                .build();

        return new CommentResponseDto(commentRepository.save(comment));
    }

    // 댓글 목록 조회 — 해당 계좌의 ACCEPT 구성원만 가능, 작성 시간 오름차순
    public List<CommentResponseDto> getComments(Long memberId, Long recordId) {
        SpendRecord spendRecord = findSpendRecord(recordId);
        validateAcceptMember(spendRecord, memberId);

        return commentRepository.findBySpendRecordIdOrderByCreatedAtAsc(recordId)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 댓글 수정 — 본인 댓글만 수정 가능, null 필드는 기존 값 유지
    @Transactional
    public CommentResponseDto updateComment(Long memberId, Long commentId,
                                            CommentUpdateRequestDto request) {
        Comment comment = findComment(commentId);
        validateAuthor(comment, memberId);

        comment.updateComment(
                request.getContent() != null ? request.getContent() : comment.getContent(),
                request.getEmoticon() != null ? request.getEmoticon() : comment.getEmoticon()
        );

        return new CommentResponseDto(comment);
    }

    // 댓글 삭제 — 본인 댓글만 삭제 가능
    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        Comment comment = findComment(commentId);
        validateAuthor(comment, memberId);
        commentRepository.delete(comment);
    }

    private SpendRecord findSpendRecord(Long recordId) {
        return spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    // SpendRecord → Transaction → Account 경로로 ACCEPT 구성원 여부 확인
    private void validateAcceptMember(SpendRecord spendRecord, Long memberId) {
        Long accountId = spendRecord.getTransaction().getAccount().getId();
        accountMemberRepository.findByAccountIdAndMemberId(accountId, memberId)
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .orElseThrow(() -> new IllegalArgumentException("해당 소비 기록에 대한 접근 권한이 없습니다."));
    }

    private void validateAuthor(Comment comment, Long memberId) {
        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 수정/삭제할 수 있습니다.");
        }
    }
}
