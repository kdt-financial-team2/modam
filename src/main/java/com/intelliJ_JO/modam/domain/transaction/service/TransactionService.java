package com.intelliJ_JO.modam.domain.transaction.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final CardRepository cardRepository;

    // 입금(DEPOSIT), 출금(WITHDRAW), 카드결제(PAYMENT) 처리 — 모든 거래의 진입점
    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto request) {
        Long memberId = request.getMemberId();

        // 1. 계좌·멤버 존재 여부 확인
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 요청한 회원이 해당 계좌의 구성원(ACCEPT)인지 검증 — WAIT/REJECT 상태면 거래 불가
        accountMemberRepository.findByAccountIdAndMemberId(request.getAccountId(), memberId)
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌에 대한 접근 권한이 없습니다."));

        TransactionType type = request.getTxType();

        // 3. PAYMENT 타입이면 cardId 필수
        if (type == TransactionType.PAYMENT && request.getCardId() == null) {
            throw new IllegalArgumentException("카드 결제 시 cardId는 필수입니다.");
        }

        // 4. cardId가 있으면 해당 카드가 이 계좌·멤버 소유인지 검증
        Card card = null;
        if (request.getCardId() != null) {
            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));

            if (!card.getAccount().getId().equals(request.getAccountId())) {
                throw new IllegalArgumentException("카드가 해당 계좌에 속하지 않습니다.");
            }
            if (!card.getMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("카드 사용 권한이 없습니다.");
            }
        }

        Long amount = request.getAmount();

        // 5. 출금·결제일 때만 잔액 부족 검사 (입금은 잔액 검사 없음)
        if ((type == TransactionType.WITHDRAW || type == TransactionType.PAYMENT)
                && account.getAvailableBalance() < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        // 6. 입금이면 +amount, 출금/결제면 -amount 로 delta 계산 후 계좌 잔액 갱신
        long delta = (type == TransactionType.DEPOSIT) ? amount : -amount;
        account.updateBalance(delta);  // balance와 availableBalance 동시 변경

        // 7. 거래 이력 저장 — afterBalance는 updateBalance() 직후 값을 스냅샷
        Transaction transaction = Transaction.builder()
                .account(account)
                .member(member)
                .card(card)
                .txType(type)
                .amount(amount)
                .afterBalance(account.getAvailableBalance())  // 거래 후 잔액 스냅샷
                .merchantName(request.getMerchantName())
                .category(request.getCategory())
                .build();

        return new TransactionResponseDto(transactionRepository.save(transaction));
    }

    // 커서 기반 페이지네이션으로 거래 내역 조회 — lastTransactionId 없으면 최신순 첫 페이지
    public List<TransactionResponseDto> getTransactions(Long accountId,
                                                        Long lastTransactionId,
                                                        int size) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다.");
        }

        Pageable pageable = PageRequest.of(0, size);
        // lastTransactionId가 null이면 전체 최신순, 있으면 해당 id 이전 데이터만 조회
        List<Transaction> transactions = (lastTransactionId == null)
                ? transactionRepository.findByAccountIdOrderByIdDesc(accountId, pageable)
                : transactionRepository.findByAccountIdAndIdLessThanOrderByIdDesc(
                        accountId, lastTransactionId, pageable);

        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
}