package com.intelliJ_JO.modam.domain.point.controller;

import com.intelliJ_JO.modam.domain.point.dto.response.PointResponse;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSpendRequest;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

// =========================================
// 🔥 Point API 기본 URL
// =========================================
@RequestMapping("/points")

@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // =========================================
    // 포인트 내역 조회
    //
    // ex)
    // GET /points/1
    // =========================================
    @GetMapping("/{memberId}")
    public List<PointResponse> getPointHistories(
            @PathVariable Long memberId
    ) {

        return pointService.getPointHistories(memberId);
    }

    // =========================================
    // 포인트 적립
    //
    // ex)
    // 출석 체크
    // 저축 목표 달성
    // 카드 결제 보상
    //
    // POST /points/save
    // =========================================
    @PostMapping("/save")
    public PointResponse savePoint(

            // =========================================
            // 🔥 @Valid 검증 추가
            //
            // DTO 유효성 검사 수행
            //
            // ex)
            // null 값 검사
            // 음수 검사
            // 빈 문자열 검사
            // =========================================
            @Valid

            // =========================================
            // 요청 Body(JSON) 받기
            // =========================================
            @RequestBody PointSaveRequest request
    ) {

        return pointService.savePoint(request);
    }

    // =========================================
    // 포인트 사용
    //
    // ex)
    // 아이템 구매
    // 테마 구매
    //
    // POST /points/spend
    // =========================================
    @PostMapping("/spend")
    public PointResponse spendPoint(

            // =========================================
            // 🔥 @Valid 검증 추가
            // =========================================
            @Valid

            // =========================================
            // 요청 Body(JSON) 받기
            // =========================================
            @RequestBody PointSpendRequest request
    ) {

        return pointService.spendPoint(request);
    }

    // =========================================
    // 현재 보유 포인트 조회
    //
    // ex)
    // GET /points/current/1
    // =========================================
    @GetMapping("/current/{memberId}")
    public Integer getCurrentPoint(
            @PathVariable Long memberId
    ) {

        return pointService.getCurrentPoint(memberId);
    }
}