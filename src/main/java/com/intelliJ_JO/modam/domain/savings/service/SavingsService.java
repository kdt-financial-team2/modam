package com.intelliJ_JO.modam.domain.savings.service;

import com.intelliJ_JO.modam.domain.savings.dto.request.SavingsCreateRequestDto;
import com.intelliJ_JO.modam.domain.savings.dto.response.SavingsResponseDto;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
// import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 데이터 읽기 전용으로 기본 세팅 (성능 최적화)
public class SavingsService {

    private final SavingsRepository savingsRepository;
    // private final AccountRepository accountRepository; // 조장님 작업 후 주석 해제!

    // 1. 새로운 저축 목표 생성 (Create)
    @Transactional // 데이터를 DB에 저장(Insert)해야 하므로 이 메서드만 readOnly 해제!
    public void createSavings(SavingsCreateRequestDto requestDto) {

        /* 🚨 [TODO] Account 리포지토리가 준비되면 주석 해제!

        // 1) 연결할 모임 통장(계좌)이 실제로 존재하는지 확인
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 통장을 찾을 수 없습니다."));

        // 2) 프론트에서 isAuto 값이 안 넘어왔다면 기본값 'N'으로 세팅
        String isAuto = (requestDto.getIsAuto() != null) ? requestDto.getIsAuto() : "N";

        // 3) 저축 엔티티 조립 (Builder 패턴 활용)
        Savings savings = Savings.builder()
                .account(account)
                .saveType(requestDto.getSaveType())
                .targetAmount(requestDto.getTargetAmount())
                .targetDate(requestDto.getTargetDate())
                .isAuto(isAuto)
                .autoAmount(requestDto.getAutoAmount())
                .autoCycle(requestDto.getAutoCycle())
                // currentAmount는 엔티티의 @Builder.Default 덕분에 자동으로 0L이 들어갑니다!
                .build();

        // 4) DB에 저장
        savingsRepository.save(savings);
        */
    }

    // 2. 특정 모임 통장의 저축 목표 목록 조회 (Read)
    public List<SavingsResponseDto> getSavingsByAccountId(Long accountId) {

        // DB에서 해당 계좌의 저축 목표들을 싹 긁어옵니다.
        List<Savings> savingsList = savingsRepository.findByAccountId(accountId);

        // 가져온 엔티티 리스트를 프론트엔드 맞춤형 DTO 리스트로 변환해서 반환합니다.
        return savingsList.stream()
                .map(SavingsResponseDto::new)
                .collect(Collectors.toList());
    }
}