package com.intelliJ_JO.modam.domain.spend.entity;

import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "spend_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SpendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래 고유번호 (tx_id) - FK, 하나의 거래 내역당 하나의 소비 기록만 존재하므로 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tx_id", nullable = false, unique = true)
    private Transaction transaction;

    // 이미지 경로 (영수증/음식 사진 등)
    @Column(name = "img_url", length = 500)
    private String imageUrl;

    // 한 줄 메모
    @Column(length = 1000)
    private String memo;

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

    // 💡 비즈니스 로직 예시: 소비 기록 내용 수정 (이미지, 메모, 이모티콘 변경)
    public void updateRecord(String imageUrl, String memo, String emoticon) {
        this.imageUrl = imageUrl;
        this.memo = memo;
        this.emoticon = emoticon;
    }
}