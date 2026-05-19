package com.intelliJ_JO.modam.domain.point.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.point.dto.response.PointResponse;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSpendRequest;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // GET /api/points — 내 포인트 내역 조회
    @GetMapping
    public GlobalResponse<List<PointResponse>> getPointHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(
                pointService.getPointHistories(userDetails.getMember().getId()));
    }

    // GET /api/points/current — 내 현재 보유 포인트 조회
    @GetMapping("/current")
    public GlobalResponse<Integer> getCurrentPoint(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(
                pointService.getCurrentPoint(userDetails.getMember().getId()));
    }

    // POST /api/points/save — 포인트 적립
    @PostMapping("/save")
    public GlobalResponse<PointResponse> savePoint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PointSaveRequest request) {
        return GlobalResponse.ok(
                pointService.savePoint(userDetails.getMember().getId(), request));
    }

    // POST /api/points/spend — 포인트 사용
    @PostMapping("/spend")
    public GlobalResponse<PointResponse> spendPoint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PointSpendRequest request) {
        return GlobalResponse.ok(
                pointService.spendPoint(userDetails.getMember().getId(), request));
    }
}
