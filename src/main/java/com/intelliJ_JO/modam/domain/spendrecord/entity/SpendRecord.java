package com.intelliJ_JO.modam.domain.spendrecord.entity;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tx_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "img_url", length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String title;

    @Column(length = 1000)
    private String memo;

    @Column(length = 50)
    private String emoticon;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateRecord(String imageUrl, String title, String memo, String emoticon) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.memo = memo;
        this.emoticon = emoticon;
    }
}
