package com.intelliJ_JO.modam.domain.card.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
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
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final AES256Util aes256Util;

    @Transactional
    public void issueCard(CardCreateRequestDto requestDto) {
        // 1) 계좌 및 멤버 조회
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2) 공통 계좌(GROUP)인지 확인 — 개인 계좌에는 카드를 발급할 수 없음
        if (account.getAccountType() != AccountType.GROUP) {
            throw new IllegalArgumentException("카드는 공통 계좌(모임 통장)에만 발급할 수 있습니다.");
        }

        // 3) 해당 계좌의 구성원인지 확인
        accountMemberRepository.findByAccountIdAndMemberId(requestDto.getAccountId(), requestDto.getMemberId())
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌의 구성원만 카드를 발급받을 수 있습니다."));

        // 4) 카드 번호 암호화
        String encryptedCardNumber = aes256Util.encrypt(requestDto.getCardNumber());

        // 5) 4자리 비밀번호 암호화 처리
        String encryptedPassword = requestDto.getPassword() != null ? aes256Util.encrypt(requestDto.getPassword()) : null;

        // 6) 카드 중복 번호 검증 한 번 더 방어 (DB에는 암호화되어 저장되므로, 암호화된 값으로 비교해야 함)
        if (cardRepository.findByCardNumber(encryptedCardNumber).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 카드 번호입니다.");
        }

        // 7) 카드 엔티티 생성
        Card card = Card.builder()
                .account(account)
                .member(member)
                .cardNumber(encryptedCardNumber) // DB에는 암호화된 번호가 들어갑니다
                .expiryDate(requestDto.getExpiryDate())
                .cardDesign(requestDto.getCardDesign())
                .cardType(requestDto.getCardType())
                .password(encryptedPassword) // 암호화된 비밀번호
                // status는 @Builder.Default 처리가 되어 있으므로 자동으로 ACTIVE가 들어갑니다
                .build();

        // 8) DB에 저장
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
    public void changeCardStatus(Long cardId, Long memberId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드를 찾을 수 없습니다."));

        if (!card.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 카드만 상태를 변경할 수 있습니다.");
        }

        card.updateStatus(newStatus);
    }
}