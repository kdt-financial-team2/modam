package com.intelliJ_JO.modam.domain.savings.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto; // 🔥 주석 해제
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType; // 🔥 주석 해제
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService; // 🔥 주석 해제
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
    private final TransactionService transactionService; // 🔥 주석 해제 완료

    /**
     * 1. 새로운 저축 목표 생성
     */
    @Transactional
    public void createSavings(SavingsCreateRequestDto requestDto) {
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));

        Savings savings = Savings.builder()
                .account(account)
                .saveType(requestDto.getSaveType())
                .targetAmount(requestDto.getTargetAmount())
                .targetDate(requestDto.getTargetDate())
                .isAuto(requestDto.getIsAuto() != null ? requestDto.getIsAuto() : "N")
                .autoAmount(requestDto.getAutoAmount())
                .autoCycle(requestDto.getAutoCycle())
                .build();

        savingsRepository.save(savings);
    }

    /**
     * 2. 특정 계좌의 저축 목표 목록 조회
     */
    public List<SavingsResponseDto> getSavingsByAccountId(Long accountId) {
        return savingsRepository.findByAccountId(accountId).stream()
                .map(SavingsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. 저축 목표에 금액 납입 (Update) 및 내역 기록
     */
    @Transactional
    public void depositToSavings(Long savingsId, Long amount, Long memberId) { // 🔥 memberId 파라미터 추가
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("납입 금액은 0원보다 커야 합니다.");
        }

        Savings savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 저축 목표입니다."));

        Account account = savings.getAccount();

        // 1. 출금 전 모임 통장 잔액 검증
        if (account.getAvailableBalance() < amount) {
            throw new IllegalStateException("모임 통장의 잔액이 부족하여 저축할 수 없습니다.");
        }

        // 2. 💡 Transaction 내역 생성 및 모임 통장 잔액 차감 (TransactionService로 위임!)
        // 기본 생성자로 빈 객체를 만든 뒤, Setter를 통해 값을 하나씩 주입합니다.
        TransactionRequestDto txRequest = new TransactionRequestDto();
        txRequest.setAccountId(account.getId());
        txRequest.setMemberId(memberId);
        // cardId는 없으므로 굳이 set 하지 않으면 기본값 null로 들어갑니다.
        txRequest.setTxType(TransactionType.WITHDRAW);
        txRequest.setAmount(amount);
        txRequest.setMerchantName("모담 저축");
        txRequest.setCategory("저축 납입");

        // 이 메서드 내부에서 account.updateBalance(-amount)가 실행되어 안전하게 잔액이 차감됩니다.
        transactionService.createTransaction(txRequest);

        // 3. 저축 목표에 금액 추가
        savings.addAmount(amount);

        // 4. 포인트 달성 여부 체크 (memberId 전달)
        checkAndAwardPoints(savings, memberId);
    }

    /**
     * ✨ 내부 비즈니스 로직: 목표 달성 여부 체크 및 포인트 지급 플래그 처리
     */
    private void checkAndAwardPoints(Savings savings, Long memberId) {
        long current = savings.getCurrentAmount();
        long target = savings.getTargetAmount();

        if (current >= (target / 2) && "N".equals(savings.getIsHalfAwarded())) {
            savings.completeHalfAward();

            // TODO: 50% 달성 포인트 지급 API 호출부
            // pointHistoryService.earnPoint(memberId, "50% 저축 달성", 100);
        }

        if (current >= target && "N".equals(savings.getIsFullAwarded())) {
            savings.completeFullAward();

            // TODO: 100% 달성 포인트 지급 API 호출부
            // pointHistoryService.earnPoint(memberId, "100% 저축 달성", 500);
        }
    }
}