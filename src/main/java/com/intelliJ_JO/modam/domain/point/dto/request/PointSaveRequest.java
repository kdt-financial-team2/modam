package com.intelliJ_JO.modam.domain.point.dto.request;

import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointSaveRequest {

    // =========================================
    // 회원 ID
    // =========================================
    private Long memberId;

    // =========================================
    // 포인트 발생 사유
    //
    // ex)
    // ATTENDANCE
    // SAVINGS_100
    // CARD_PAYMENT
    // =========================================
    private PointReason reason;

    // =========================================
    // 적립 포인트
    //
    // ex)
    // 100
    // 3000
    // =========================================
    private Integer amt;

    // =========================================
    // 포인트 설명
    //
    // ex)
    // "출석 체크 보상"
    // =========================================
    private String descrip;
}