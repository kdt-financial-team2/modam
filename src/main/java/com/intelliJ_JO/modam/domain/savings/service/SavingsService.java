package com.intelliJ_JO.modam.domain.savings.service;

import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final SavingsRepository savingsRepository;


    private final AccountRepository accountRepository;

    /**
     * 1. 새로운 저축 목표 생성
     */
    @Transactional
    public SavingsResponseDto createSavings(SavingsCreateRequestDto requestDto) {
        // TODO: Account 조회 로직 연결 필요 (현재는 연관관계 세팅 보류)
        // Account account = accountRepository.findById(requestDto.getAccountId()).orElseThrow(...);

        Savings savings = Savings.builder()
                // .account(account)
                .saveType(requestDto.getSaveType())
                .targetAmount(requestDto.getTargetAmount()) // ✨ 풀네임 수정
                .targetDate(requestDto.getTargetDate())     // ✨ 풀네임 수정
                .isAuto(requestDto.getIsAuto() != null ? requestDto.getIsAuto() : "N")
                .autoAmount(requestDto.getAutoAmount())     // ✨ 풀네임 수정
                .autoCycle(requestDto.getAutoCycle())
                .currentAmount(0L)                          // ✨ 풀네임 수정 (초기 금액 0원)
                .build();

        Savings savedSavings = savingsRepository.save(savings);
        return new SavingsResponseDto(savedSavings);
    }

    /**
     * 2. 특정 계좌의 저축 목표 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SavingsResponseDto> getSavingsByAccountId(Long accountId) {
        return savingsRepository.findByAccountId(accountId).stream()
                .map(SavingsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. 저축 목표에 금액 납입 (Update)
     * - 돈을 납입하면 현재 모인 금액(currentAmount)이 증가합니다.
     */
    @Transactional
    public void depositToSavings(Long savingsId, Long amount) {
        // 1. 납입하려는 금액이 유효한지 1차 검증
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("납입 금액은 0원보다 커야 합니다.");
        }

        // 2. 납입할 저축 목표 엔티티 조회
        Savings savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 저축 목표입니다."));

        // 3. 💡 향후 고도화 포인트: 실제 Account(모임 통장) 원장에서 돈을 빼는 로직이 여기에 추가되어야 합니다!
        // accountService.withdraw(savings.getAccount().getId(), amount);

        // 4. 저축 엔티티의 현재 모인 금액 증가 (엔티티 내부에 만들어둔 메서드 호출)
        savings.addAmount(amount);

        // 더티 체킹(Dirty Checking) 덕분에 별도의 save() 호출 없이도 DB에 자동 업데이트 됩니다!
    }
}