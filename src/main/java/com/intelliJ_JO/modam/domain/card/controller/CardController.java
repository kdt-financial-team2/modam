package com.intelliJ_JO.modam.domain.card.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.CardResponseDto;
import com.intelliJ_JO.modam.domain.card.entity.CardStatus;
import com.intelliJ_JO.modam.domain.card.service.CardService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카드", description = "카드 발급/조회/상태 변경 API")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "카드 발급")
    @PostMapping
    public GlobalResponse<Void> issueCard(@Valid @RequestBody CardCreateRequestDto requestDto) {
        cardService.issueCard(requestDto);
        return GlobalResponse.ok("카드가 성공적으로 발급되었습니다.");
    }

    @Operation(summary = "계좌별 카드 목록 조회")
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<CardResponseDto>> getCardsByAccountId(@PathVariable Long accountId) {
        return GlobalResponse.ok(cardService.getCardsByAccountId(accountId));
    }

    @Operation(summary = "카드 상태 변경 (분실 신고, 일시 정지 등)")
    @PatchMapping("/{cardId}/status")
    public GlobalResponse<Void> changeCardStatus(
            @PathVariable Long cardId,
            @RequestParam CardStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cardService.changeCardStatus(cardId, userDetails.getMember().getId(), status);
        return GlobalResponse.ok("카드 상태가 성공적으로 변경되었습니다.");
    }
}
