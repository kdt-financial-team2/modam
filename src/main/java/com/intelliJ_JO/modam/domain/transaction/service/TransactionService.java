package com.intelliJ_JO.modam.domain.transaction.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.request.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.dto.response.TransactionResponseDto;
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
    private final MemberRepository memberRepository;
    private final CardRepository cardRepository;

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Card card = null;
        if (request.getCardId() != null) {
            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));
        }

        TransactionType type = request.getTxType();
        Long amount = request.getAmount();

        if ((type == TransactionType.WITHDRAW || type == TransactionType.PAYMENT)
                && account.getAvailableBalance() < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        long delta = (type == TransactionType.DEPOSIT) ? amount : -amount;
        account.updateBalance(delta);

        Transaction transaction = Transaction.builder()
                .account(account)
                .member(member)
                .card(card)
                .txType(type)
                .amount(amount)
                .afterBalance(account.getAvailableBalance())
                .merchantName(request.getMerchantName())
                .category(request.getCategory())
                .build();

        return new TransactionResponseDto(transactionRepository.save(transaction));
    }

    public List<TransactionResponseDto> getTransactions(Long accountId,
                                                        Long lastTransactionId,
                                                        int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<Transaction> transactions = (lastTransactionId == null)
                ? transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                : transactionRepository.findByAccountIdAndIdLessThanOrderByCreatedAtDesc(
                        accountId, lastTransactionId, pageable);

        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
}