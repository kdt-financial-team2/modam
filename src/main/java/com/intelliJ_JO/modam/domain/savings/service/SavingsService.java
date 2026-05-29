package com.intelliJ_JO.modam.domain.savings.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.entity.AutoCycle;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsService {

    private final SavingsRepository savingsRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final PointService pointService;

    @Transactional
    public void createSavings(SavingsCreateRequestDto requestDto) {
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));

        Savings savings = Savings.builder()
                .account(account)
                .goalName(requestDto.getGoalName())
                .saveType(requestDto.getSaveType())
                .targetAmount(requestDto.getTargetAmount())
                .targetDate(requestDto.getTargetDate())
                .isAuto(requestDto.getIsAuto() != null ? requestDto.getIsAuto() : "N")
                .autoAmount(requestDto.getAutoAmount())
                .autoCycle(requestDto.getAutoCycle())
                .myContribution(requestDto.getMyContribution())
                .partnerContribution(requestDto.getPartnerContribution())
                .build();

        savingsRepository.save(savings);
    }

    public List<SavingsResponseDto> getSavingsByAccountId(Long accountId) {
        return savingsRepository.findByAccountId(accountId).stream()
                .map(SavingsResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void depositToSavings(Long savingsId, Long amount, Long memberId) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("납입 금액은 0원보다 커야 합니다.");
        }

        Savings savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 저축 목표입니다."));

        Account account = savings.getAccount();

        if (account.getAvailableBalance() < amount) {
            throw new IllegalStateException("모임 통장의 잔액이 부족하여 저축할 수 없습니다.");
        }

        TransactionRequestDto txRequest = new TransactionRequestDto();
        txRequest.setAccountId(account.getId());
        txRequest.setMemberId(memberId);
        txRequest.setTxType(TransactionType.WITHDRAW);
        txRequest.setAmount(amount);
        txRequest.setMerchantName("모담 저축");
        txRequest.setCategory("저축 납입");

        transactionService.createTransaction(txRequest);
        savings.addAmount(amount);
        checkAndAwardPoints(savings, memberId);
    }

    @Transactional
    public void updateAutoTransfer(Long goalId, Long amount, String frequency, String startDate) {
        Savings savings = savingsRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 저축 목표입니다."));

        AutoCycle cycle = AutoCycle.valueOf(frequency.toUpperCase());

        savings.updateAutoTransfer(amount, cycle);
        savingsRepository.save(savings);
    }

    @Transactional
    public void deleteSavings(Long savingsId) {
        Savings savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 저축 목표입니다."));
        savingsRepository.delete(savings);
    }

    private void checkAndAwardPoints(Savings savings, Long memberId) {
        long current = savings.getCurrentAmount();
        long target = savings.getTargetAmount();

        Long accountId = savings.getAccount().getId();

        if (current >= (target / 2) && "N".equals(savings.getIsHalfAwarded())) {
            savings.completeHalfAward();
            pointService.savePointByAccountId(accountId, PointSaveRequest.builder()
                    .reason(PointReason.SAVINGS_50)
                    .amt(100)
                    .descrip("저축 목표 50% 달성 보상")
                    .build());
        }

        if (current >= target && "N".equals(savings.getIsFullAwarded())) {
            savings.completeFullAward();
            pointService.savePointByAccountId(accountId, PointSaveRequest.builder()
                    .reason(PointReason.SAVINGS_100)
                    .amt(500)
                    .descrip("저축 목표 100% 달성 보상")
                    .build());

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            String msg = String.format("저축 목표 달성! %,d원이 모두 적립되었습니다.", target);
            notificationService.send(member, NotificationType.SAVINGS_GOAL, msg,
                    "/savings/" + savings.getId());
        }
    }
}