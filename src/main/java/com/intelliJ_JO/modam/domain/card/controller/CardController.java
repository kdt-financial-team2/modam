package com.intelliJ_JO.modam.domain.card.controller;

import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.CardResponseDto;
import com.intelliJ_JO.modam.domain.card.entity.CardStatus;
import com.intelliJ_JO.modam.domain.card.service.CardService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 1. 카드 발급
    @PostMapping
    public GlobalResponse<Void> issueCard(@Valid @RequestBody CardCreateRequestDto requestDto) {
        cardService.issueCard(requestDto);
        return GlobalResponse.ok("카드가 성공적으로 발급되었습니다.");
    }

    // 2. 특정 모임 통장의 카드 목록 조회
    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<CardResponseDto>> getCardsByAccountId(@PathVariable Long accountId) {
        return GlobalResponse.ok(cardService.getCardsByAccountId(accountId));
    }

    // 3. 카드 상태 변경 (분실 신고, 일시 정지 등)
    // 💡 URL 경로 예시: PATCH "/api/cards/1/status?memberId=1&status=LOST"
    @PatchMapping("/{cardId}/status")
    public GlobalResponse<Void> changeCardStatus(
            @PathVariable Long cardId,
            @RequestParam Long memberId,
            @RequestParam CardStatus status) {

        cardService.changeCardStatus(cardId, memberId, status);
        return GlobalResponse.ok("카드 상태가 성공적으로 변경되었습니다.");
    }
}