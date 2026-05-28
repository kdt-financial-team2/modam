package com.intelliJ_JO.modam.global.init;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTransactionsForNewAccounts();
    }

    private void seedTransactionsForNewAccounts() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (transactionRepository.existsByAccountId(account.getId())) continue;

            List<AccountMember> accepted = accountMemberRepository
                    .findByAccountIdAndInviteStatus(account.getId(), InviteStatus.ACCEPT);
            if (accepted.isEmpty()) continue;

            Member member = accepted.get(0).getMember();
            createTransactions(account, member);
            log.info("[DataInitializer] 계좌 {} 에 거래 내역 샘플 데이터 삽입.", account.getAccountNumber());
        }
    }

    private void createTransactions(Account account, Member member) {
        long totalSpent = 145_000L + 45_000L + 17_000L + 32_000L + 8_500L
                        + 56_000L + 100_000L + 28_000L + 89_000L;
        long bal = account.getBalance() + totalSpent;

        List<Transaction> txList = List.of(
                tx(account, member, TransactionType.DEPOSIT,  "입금",      null,         bal,               bal),
                tx(account, member, TransactionType.PAYMENT,  "식료품",    "이마트",     145_000L, bal -= 145_000L),
                tx(account, member, TransactionType.PAYMENT,  "카페/음료", "스타벅스",    45_000L, bal -=  45_000L),
                tx(account, member, TransactionType.PAYMENT,  "구독",      "넷플릭스",    17_000L, bal -=  17_000L),
                tx(account, member, TransactionType.PAYMENT,  "식비",      "배달의민족",  32_000L, bal -=  32_000L),
                tx(account, member, TransactionType.PAYMENT,  "편의점",    "GS25",         8_500L, bal -=   8_500L),
                tx(account, member, TransactionType.PAYMENT,  "뷰티",      "올리브영",    56_000L, bal -=  56_000L),
                tx(account, member, TransactionType.WITHDRAW, "현금출금",  "ATM",        100_000L, bal -= 100_000L),
                tx(account, member, TransactionType.PAYMENT,  "여가",      "CGV",         28_000L, bal -=  28_000L),
                tx(account, member, TransactionType.PAYMENT,  "식료품",    "마켓컬리",    89_000L, bal -=  89_000L)
        );
        transactionRepository.saveAll(txList);
    }

    private Transaction tx(Account account, Member member, TransactionType type,
                           String category, String merchantName, Long amount, Long afterBalance) {
        return Transaction.builder()
                .account(account)
                .member(member)
                .txType(type)
                .category(category)
                .merchantName(merchantName)
                .amount(amount)
                .afterBalance(afterBalance)
                .build();
    }
}
