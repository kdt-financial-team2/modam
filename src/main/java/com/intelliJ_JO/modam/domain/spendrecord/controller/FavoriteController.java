package com.intelliJ_JO.modam.domain.spendrecord.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.spendrecord.service.FavoriteService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "즐겨찾기", description = "소비 스토리 즐겨찾기 토글 API")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 즐겨찾기 토글 — 추가 시 favorited:true, 해제 시 favorited:false 반환
     */
    @Operation(summary = "즐겨찾기 토글")
    @PostMapping("/{recordId}")
    public GlobalResponse<Map<String, Boolean>> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId) {
        boolean favorited = favoriteService.toggle(userDetails.getMember().getId(), recordId);
        return GlobalResponse.ok(Map.of("favorited", favorited));
    }
}
