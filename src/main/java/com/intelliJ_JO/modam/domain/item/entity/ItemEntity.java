package com.intelliJ_JO.modam.domain.item.entity;

import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "item_seq_generator",
        sequenceName = "item_seq",
        allocationSize = 1
)
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_seq_generator")
    private Long id;

    // ==============================
    // 아이템 이름
    // ==============================
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    // ==============================
    // 아이템 종류 (THEME, EMOJI 등)
    // ==============================
    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    // ==============================
    // 가격
    // ==============================
    @Column(name = "price", nullable = false)
    private Integer price;

    // ==============================
    // 이미지 경로
    // ==============================
    @Column(name = "img_url")
    private String imgUrl;

    // =========================================================
    // [수정 포인트] 활성 여부 기존 String "Y/N" 방식 → ENUM으로 변경
    //
    // 기존 방식 문제:
    // - "Y", "N" 오타 위험
    // - 의미 불명확
    // - 유지보수 어려움
    //
    // 개선 방식:
    // - enum으로 상태를 명확하게 관리
    // =========================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private ItemStatus isActive = ItemStatus.ACTIVE;

    // ==============================
    // 생성일
    // ==============================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==============================
    // 수정일
    // ==============================
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==============================
    // INSERT 시 자동 실행
    // ==============================
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        // 기본값 보장 (null 방지)
        if (this.isActive == null) {
            this.isActive = ItemStatus.ACTIVE;
        }
    }

    // ==============================
    // UPDATE 시 자동 실행
    // ==============================
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}