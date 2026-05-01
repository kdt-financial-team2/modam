package com.intelliJ_JO.modam.domain.transaction.service;

import com.intelliJ_JO.modam.domain.transaction.dto.request.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    // private final AccountRepository accountRepository; // 조장님이 만드시면 주석 해제!
    // private final MemberRepository memberRepository;

    @Transactional
    public void createTransaction(TransactionRequestDto requestDto) {

        /*
         * 🚨 [TODO] 조장님이 Account, Member 엔티티를 완성하면 아래 주석을 풀고 연동합니다!
         *
         * 1. DB에서 계좌와 멤버 객체를 꺼내옵니다.
         * Account account = accountRepository.findById(requestDto.getAccountId())
         *         .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
         * Member member = memberRepository.findById(requestDto.getMemberId())
         *         .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
         *
         * 2. 거래 유형(입금/출금/결제)에 따라 계좌 잔액(account.balance)을 더하거나 뺍니다.
         *    (출금이나 결제일 경우 잔액이 부족하면 예외를 발생시키는 로직 필수!)
         *
         * 3. 계산된 최종 잔액(afterBalance)을 포함하여 Transaction 엔티티를 조립합니다.
         * Transaction transaction = Transaction.builder()
         *         .account(account)
         *         .member(member)
         *         .transactionType(requestDto.getTransactionType())
         *         .amount(requestDto.getAmount())
         *         .afterBalance(계산된_최종_잔액) // 중요!
         *         .merchantName(requestDto.getMerchantName())
         *         .category(requestDto.getCategory())
         *         .build();
         *
         * 4. 내역을 DB에 저장합니다.
         * transactionRepository.save(transaction);
         */
    }
}
