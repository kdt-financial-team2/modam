package com.intelliJ_JO.modam.domain.card.controller;

import com.intelliJ_JO.modam.domain.card.dto.request.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.response.CardResponseDto;
import com.intelliJ_JO.modam.domain.card.service.CardService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 1. 새로운 카드 발급
    @PostMapping
    public ResponseEntity<GlobalResponse<Void>> issueCard(@Valid @RequestBody CardCreateRequestDto requestDto) {
        cardService.issueCard(requestDto);
        return ResponseEntity.ok(GlobalResponse.ok("카드가 성공적으로 발급되었습니다."));
    }

    // 2. 특정 모임 통장(계좌)에 연결된 카드 목록 조회
    // 💡 URL 경로 꿀팁: "/api/cards/{id}" 대신 "/account/{accountId}"로 명시하여
    // 나중에 "특정 카드 1개 조회(/api/cards/{cardId})" API가 추가될 때 충돌하지 않도록 방어했습니다!
    @GetMapping("/account/{accountId}")
    public ResponseEntity<GlobalResponse<List<CardResponseDto>>> getCardsByAccountId(@PathVariable Long accountId) {
        List<CardResponseDto> cardList = cardService.getCardsByAccountId(accountId);
        return ResponseEntity.ok(GlobalResponse.ok("카드 목록 조회 성공", cardList));
    }
}