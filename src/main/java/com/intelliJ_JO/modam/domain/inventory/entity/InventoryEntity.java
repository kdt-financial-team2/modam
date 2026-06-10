package com.intelliJ_JO.modam.domain.inventory.entity;

import com.intelliJ_JO.modam.domain.inventory.enums.ApplyStatus;
import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                // 한 사람이 같은 아이템을 중복으로 가지지 못하게 막음
                @UniqueConstraint(name = "uk_mem_item", columnNames = {"mem_id", "item_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    // 아이템 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

    // 적용 상태 (APPLIED, NOT_APPLIED)
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_status", nullable = false)
    @Builder.Default
    private ApplyStatus applyStatus = ApplyStatus.NOT_APPLIED;

    // 생성일
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일, 장착/해제 등 변경된 시간
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 데이터가 처음 저장될 때 자동 실행
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();}

    // 데이터가 수정될 때 자동 실행
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}