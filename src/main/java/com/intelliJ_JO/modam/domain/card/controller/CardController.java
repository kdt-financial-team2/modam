package com.intelliJ_JO.modam.domain.card.controller;

import com.intelliJ_JO.modam.domain.card.dto.request.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.response.CardResponseDto;
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

    @PostMapping
    public GlobalResponse<Void> issueCard(@Valid @RequestBody CardCreateRequestDto requestDto) {
        cardService.issueCard(requestDto);
        return GlobalResponse.ok("카드가 성공적으로 발급되었습니다.");
    }

    @GetMapping("/account/{accountId}")
    public GlobalResponse<List<CardResponseDto>> getCardsByAccountId(@PathVariable Long accountId) {
        return GlobalResponse.ok(cardService.getCardsByAccountId(accountId));
    }
}
