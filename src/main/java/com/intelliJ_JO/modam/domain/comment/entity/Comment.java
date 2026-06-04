package com.intelliJ_JO.modam.domain.comment.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "record_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소비 기록 번호 (record_id) - FK, 다대일(N:1) 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private SpendRecord spendRecord;

    // 작성자 고유번호 (mem_id) - FK, 다대일(N:1) 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 내용
    @Column(length = 500, nullable = false)
    private String content;

    // 사용 이모티콘
    @Column(length = 50)
    private String emoticon;

    // 작성 일시
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 수정 일시
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 💡 비즈니스 로직 예시: 댓글 내용 및 이모티콘 수정
    public void updateComment(String content, String emoticon) {
        this.content = content;
        this.emoticon = emoticon;
    }
}