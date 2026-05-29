package com.intelliJ_JO.modam.domain.point.dto.response;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointResponse {
    // =========================================
    // 포인트 내역 ID
    // =========================================
    private Long id;
    private Long coupleId;
    // =========================================
    // 포인트 타입
    //
    // SAVE / SPEND
    // =========================================
    private PointType type;
    // =========================================
    // 포인트 발생 사유
    // =========================================
    private PointReason reason;
    // =========================================
    // 포인트 변화량
    // =========================================
    private Integer amt;
    // =========================================
    // 포인트 반영 후 잔액
    // =========================================
    private Integer aftBal;
    // =========================================
    // 포인트 설명
    // =========================================
    private String descrip;
    // =========================================
    // 생성일
    // =========================================
    private LocalDateTime createdAt;
}