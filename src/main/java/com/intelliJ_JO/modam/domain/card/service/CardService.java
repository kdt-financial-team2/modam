package com.intelliJ_JO.modam.domain.card.service;

import com.intelliJ_JO.modam.domain.card.dto.request.CardCreateRequestDto;
import com.intelliJ_JO.modam.domain.card.dto.response.CardResponseDto;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
// import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
// import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 데이터를 읽기만 하는 메서드가 많을 때 성능을 높여줍니다.
public class CardService {

    private final CardRepository cardRepository;
    // private final AccountRepository accountRepository; // 조장님 작업 후 주석 해제!
    // private final MemberRepository memberRepository;   // 조장님 작업 후 주석 해제!

    // 1. 카드 발급 (Create)
    @Transactional // 데이터를 변경(Insert)하므로 readOnly = false 역할
    public void issueCard(CardCreateRequestDto requestDto) {

        /* 🚨 [TODO] Account, Member 리포지토리가 준비되면 주석 해제하여 사용하세요!

        // 1) 계좌와 멤버가 실제로 존재하는지 검증
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2) 중복된 카드 번호가 있는지 한 번 더 방어 (선택 사항)
        if (cardRepository.findByCardNumber(requestDto.getCardNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 카드 번호입니다.");
        }

        // 3) 카드 엔티티 생성
        Card card = Card.builder()
                .account(account)
                .member(member)
                .cardNumber(requestDto.getCardNumber())
                .expiryDate(requestDto.getExpiryDate())
                .status("ACTIVE") // 카드를 처음 발급하면 상태는 무조건 '정상(ACTIVE)'
                .build();

        // 4) DB에 저장
        cardRepository.save(card);
        */
    }

    // 2. 모임 통장(계좌)에 연결된 카드 목록 조회 (Read)
    public List<CardResponseDto> getCardsByAccountId(Long accountId) {
        // DB에서 계좌 ID로 카드 목록을 싹 가져옵니다.
        List<Card> cards = cardRepository.findByAccountId(accountId);

        // 가져온 Card 엔티티들을 프론트엔드에 줄 CardResponseDto로 변환해서 리턴합니다.
        return cards.stream()
                .map(CardResponseDto::new) // 우리가 아까 만든 DTO 생성자 활용!
                .collect(Collectors.toList());
    }
}