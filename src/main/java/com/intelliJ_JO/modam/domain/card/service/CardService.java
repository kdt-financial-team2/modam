package com.intelliJ_JO.modam.domain.card.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.CardResponseDto;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.entity.CardStatus;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.global.util.AES256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository; // 봉인 해제!
    private final MemberRepository memberRepository;   // 봉인 해제!
    private final AES256Util aes256Util;             // 🔥 암호화 유틸리티 주입!

    /**
     * 1. 카드 발급 (Create)
     */
    @Transactional
    public void issueCard(CardCreateRequestDto requestDto) {

        // 1) 계좌와 멤버가 실제로 존재하는지 검증
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2) 💡 프론트에서 넘어온 평문 카드 번호를 AES-256으로 암호화
        String encryptedCardNumber = aes256Util.encrypt(requestDto.getCardNumber());

        // 3) 중복된 카드 번호가 있는지 한 번 더 방어 (DB에는 암호화되어 저장되므로, 암호화된 값으로 비교해야 함)
        if (cardRepository.findByCardNumber(encryptedCardNumber).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 카드 번호입니다.");
        }

        // 4) 카드 엔티티 생성
        Card card = Card.builder()
                .account(account)
                .member(member)
                .cardNumber(encryptedCardNumber) // 🔥 DB에는 암호화된 번호가 들어갑니다!
                .expiryDate(requestDto.getExpiryDate())
                // status는 @Builder.Default 처리가 되어 있으므로 자동으로 ACTIVE가 들어갑니다.
                .build();

        // 5) DB에 저장
        cardRepository.save(card);
    }

    /**
     * 2. 모임 통장(계좌)에 연결된 카드 목록 조회 (Read)
     */
    public List<CardResponseDto> getCardsByAccountId(Long accountId) {
        List<Card> cards = cardRepository.findByAccountId(accountId);

        return cards.stream()
                .map(CardResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. 카드 상태 변경 (Update) - 분실(LOST) 또는 정지(STOPPED) 처리
     */
    @Transactional
    public void changeCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드를 찾을 수 없습니다."));

        card.updateStatus(newStatus);
    }
}