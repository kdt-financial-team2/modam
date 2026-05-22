package com.intelliJ_JO.modam.global.init;

import com.intelliJ_JO.modam.domain.account.entity.*;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.repository.NotificationRepository;
import com.intelliJ_JO.modam.domain.point.entity.PointHistory;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import com.intelliJ_JO.modam.domain.point.repository.PointRepository;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import com.intelliJ_JO.modam.domain.spend.entity.SpendingLimit;
import com.intelliJ_JO.modam.domain.spend.repository.SpendingLimitRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final CoupleRepository coupleRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsRepository savingsRepository;
    private final PointRepository pointRepository;
    private final NotificationRepository notificationRepository;
    private final SpendingLimitRepository spendingLimitRepository;

    private static final String TARGET_USER_ID = "2222";
    private static final String DUMMY_ACCT_NO  = "110-2222-333344";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<Member> memberOpt = memberRepository.findByUserId(TARGET_USER_ID);
        if (memberOpt.isEmpty()) {
            log.info("[DataInitializer] user_id='{}' 인 회원이 없어 더미 데이터를 건너뜁니다.", TARGET_USER_ID);
            return;
        }
        if (accountRepository.existsByAccountNumber(DUMMY_ACCT_NO)) {
            log.info("[DataInitializer] 더미 데이터가 이미 존재합니다. 건너뜁니다.");
            return;
        }

        Member member = memberOpt.get();
        log.info("[DataInitializer] '{}' 회원에게 더미 데이터를 삽입합니다.", member.getName());

        Account account = createAccount();
        createAccountMember(account, member);
        createCouple(account);
        createTransactions(account, member);
        createSavings(account);
        createPointHistory(member);
        createNotifications(member);
        createSpendingLimits(member);

        log.info("[DataInitializer] 더미 데이터 삽입 완료.");
    }

    // ===== 계좌 =====
    private Account createAccount() {
        return accountRepository.save(Account.builder()
                .accountNumber(DUMMY_ACCT_NO)
                .accountType(AccountType.GROUP)
                .status(AccountStatus.ACTIVE)
                .balance(3_500_000L)
                .availableBalance(3_500_000L)
                .spendLimitAmount(1_000_000L)
                .onceTransferLimit(500_000L)
                .dailyTransferLimit(2_000_000L)
                .build());
    }

    // ===== 계좌-회원 매핑 =====
    private void createAccountMember(Account account, Member member) {
        AccountMember am = AccountMember.builder()
                .account(account)
                .member(member)
                .inviteStatus(InviteStatus.ACCEPT)
                .totalDeposit(3_000_000L)
                .build();
        am.acceptInvite();
        accountMemberRepository.save(am);
    }

    // ===== 커플 정보 =====
    private void createCouple(Account account) {
        coupleRepository.save(Couple.builder()
                .account(account)
                .inviteCode("MODAM2222")
                .dDay(LocalDate.of(2024, 3, 14))
                .accountAlias("우리의 달콤한 저금통")
                .build());
    }

    // ===== 거래 내역 =====
    private void createTransactions(Account account, Member member) {
        List<Transaction> txList = List.of(
                tx(account, member, TransactionType.DEPOSIT,  "입금",      null,        3_000_000L, 6_500_000L),
                tx(account, member, TransactionType.PAYMENT,  "식료품",    "이마트",     145_000L,  6_355_000L),
                tx(account, member, TransactionType.PAYMENT,  "카페/음료", "스타벅스",    45_000L,  6_310_000L),
                tx(account, member, TransactionType.PAYMENT,  "구독",      "넷플릭스",    17_000L,  6_293_000L),
                tx(account, member, TransactionType.PAYMENT,  "식비",      "배달의민족",  32_000L,  6_261_000L),
                tx(account, member, TransactionType.PAYMENT,  "편의점",    "GS25",        8_500L,  6_252_500L),
                tx(account, member, TransactionType.PAYMENT,  "뷰티",      "올리브영",    56_000L,  6_196_500L),
                tx(account, member, TransactionType.WITHDRAW, "현금출금",  "ATM",        100_000L,  6_096_500L),
                tx(account, member, TransactionType.PAYMENT,  "여가",      "CGV",         28_000L,  6_068_500L),
                tx(account, member, TransactionType.PAYMENT,  "식료품",    "마켓컬리",    89_000L,  5_979_500L)
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

    // ===== 목표 저축 =====
    private void createSavings(Account account) {
        Savings savings = Savings.builder()
                .account(account)
                .saveType("여행")
                .targetAmount(2_000_000L)
                .currentAmount(850_000L)
                .targetDate(LocalDate.of(2026, 12, 31))
                .isAuto("Y")
                .autoAmount(100_000L)
                .build();
        savingsRepository.save(savings);
    }

    // ===== 포인트 내역 =====
    private void createPointHistory(Member member) {
        List<PointHistory> histories = List.of(
                point(member, PointType.SAVE, PointReason.ATTENDANCE,    100, 100,  "출석 체크 보상"),
                point(member, PointType.SAVE, PointReason.CARD_PAYMENT,   50, 150,  "카드 결제 적립"),
                point(member, PointType.SAVE, PointReason.SAVINGS_50,    500, 650,  "저축 목표 50% 달성 보상"),
                point(member, PointType.SAVE, PointReason.SPEND_RECORD,   30, 680,  "소비 내역 기록 보상"),
                point(member, PointType.SAVE, PointReason.ATTENDANCE,    100, 780,  "출석 체크 보상")
        );
        pointRepository.saveAll(histories);
    }

    private PointHistory point(Member member, PointType type, PointReason reason,
                               int amt, int aftBal, String descrip) {
        return PointHistory.builder()
                .member(member)
                .type(type)
                .reason(reason)
                .amt(amt)
                .aftBal(aftBal)
                .descrip(descrip)
                .build();
    }

    // ===== 알림 =====
    private void createNotifications(Member member) {
        List<Notification> notiList = List.of(
                noti(member, NotificationType.DEPOSIT,
                        "3,000,000원이 공동 계좌에 입금되었습니다.", "/dashboard"),
                noti(member, NotificationType.SAVINGS_GOAL,
                        "제주도 여행 저축 목표의 50%를 달성했어요!", "/savings"),
                noti(member, NotificationType.LIMIT_WARNING,
                        "카페/음료 예산의 90%를 사용했습니다. 주의하세요!", "/spending-limit")
        );
        notificationRepository.saveAll(notiList);
    }

    private Notification noti(Member member, NotificationType type, String message, String url) {
        return Notification.builder()
                .member(member)
                .notiType(type)
                .message(message)
                .targetUrl(url)
                .build();
    }

    // ===== 소비 제한 =====
    private void createSpendingLimits(Member member) {
        List<SpendingLimit> limits = List.of(
                limit(member, "식비",      600_000L),
                limit(member, "카페/음료", 200_000L),
                limit(member, "여가",      300_000L),
                limit(member, "식료품",    500_000L)
        );
        spendingLimitRepository.saveAll(limits);
    }

    private SpendingLimit limit(Member member, String category, Long amount) {
        return SpendingLimit.builder()
                .member(member)
                .category(category)
                .budgetAmount(amount)
                .build();
    }
}
