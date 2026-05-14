package com.intelliJ_JO.modam.domain.spend.service;

import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordResponseDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spend.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendRecordService {

    private final SpendRecordRepository spendRecordRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public SpendRecordResponseDto createSpendRecord(SpendRecordCreateRequestDto request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다."));

        // 동일 거래에 소비 기록이 이미 존재하면 중복 생성 불가
        if (spendRecordRepository.existsByTransactionId(request.getTransactionId())) {
            throw new IllegalStateException("이미 해당 거래에 소비 기록이 존재합니다.");
        }

        SpendRecord spendRecord = SpendRecord.builder()
                .transaction(transaction)
                .imageUrl(request.getImageUrl())
                .memo(request.getMemo())
                .emoticon(request.getEmoticon())
                .build();

        return new SpendRecordResponseDto(spendRecordRepository.save(spendRecord));
    }

    public SpendRecordResponseDto getSpendRecordByTransaction(Long transactionId) {
        SpendRecord spendRecord = spendRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래의 소비 기록을 찾을 수 없습니다."));
        return new SpendRecordResponseDto(spendRecord);
    }

    @Transactional
    public SpendRecordResponseDto updateSpendRecord(Long recordId, SpendRecordUpdateRequestDto request) {
        SpendRecord spendRecord = spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));

        // null이면 기존 값 유지 (PATCH 의미)
        spendRecord.updateRecord(
                request.getImageUrl() != null ? request.getImageUrl() : spendRecord.getImageUrl(),
                request.getMemo() != null ? request.getMemo() : spendRecord.getMemo(),
                request.getEmoticon() != null ? request.getEmoticon() : spendRecord.getEmoticon()
        );

        return new SpendRecordResponseDto(spendRecord);
    }

    @Transactional
    public void deleteSpendRecord(Long recordId) {
        SpendRecord spendRecord = spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));
        spendRecordRepository.delete(spendRecord);
    }
}
