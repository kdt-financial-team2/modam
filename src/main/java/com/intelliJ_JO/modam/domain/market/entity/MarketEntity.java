package com.intelliJ_JO.modam.domain.market.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "market")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "market_seq_generator",
        sequenceName = "market_seq",
        allocationSize = 1
)


public class MarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "market_seq_generator")
    private Long id;

    // 아이템 이름
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    // 아이템 종류 (THEME, EMOJI)
    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    // 가격
    @Column(name = "price", nullable = false)
    private Integer price;

    // 이미지 경로
    @Column(name = "img_url")
    private String imgUrl;

    // 활성 여부
    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive = "Y";

    // 생성일
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 시 자동 세팅
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = "Y";
        }
    }

    // 수정 시 자동 세팅
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
