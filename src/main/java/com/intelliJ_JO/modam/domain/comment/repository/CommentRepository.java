package com.intelliJ_JO.modam.domain.comment.repository;

import com.intelliJ_JO.modam.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findBySpendRecordIdOrderByCreatedAtAsc(Long spendRecordId);
}
