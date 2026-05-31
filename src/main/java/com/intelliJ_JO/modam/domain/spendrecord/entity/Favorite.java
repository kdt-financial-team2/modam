package com.intelliJ_JO.modam.domain.spendrecord.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 소비 스토리 즐겨찾기 — 회원(Member)과 소비기록(SpendRecord)의 N:M 관계를 담당
 * 동일 회원이 동일 기록에 중복 즐겨찾기 불가 (unique 제약)
 */
@Entity
@Table(name = "favorite",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mem_id", "record_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 즐겨찾기를 누른 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 즐겨찾기 대상 소비기록
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private SpendRecord spendRecord;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
