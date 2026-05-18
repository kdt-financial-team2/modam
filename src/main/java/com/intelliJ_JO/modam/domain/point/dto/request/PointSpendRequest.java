package com.intelliJ_JO.modam.domain.point.dto.request;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointSpendRequest {
    // =========================================
    // 회원 ID
    // =========================================
    private Long memberId;
    // =========================================
    // 포인트 사용 사유
    //
    // ex)
    // ITEM_PURCHASE
    // THEME_PURCHASE
    // =========================================
    private PointReason reason;
    // =========================================
    // 사용 포인트
    //
    // ex)
    // 500
    // 3000
    // =========================================
    private Integer amt;
    // =========================================
    // 포인트 사용 설명
    //
    // ex)
    // "아이템 구매"
    // =========================================
    private String descrip;
}