package com.intelliJ_JO.modam.domain.card.repository;

import com.intelliJ_JO.modam.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // 특정 모임 통장(계좌)에 연결된 모든 카드 목록 조회
    List<Card> findByAccountId(Long accountId);

    // 특정 멤버가 발급받은 카드 목록 조회
    List<Card> findByMemberId(Long memberId);

    // 카드 번호로 특정 카드 단건 조회 (결제 시 검증용)
    Optional<Card> findByCardNumber(String cardNumber);
}