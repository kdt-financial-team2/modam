package com.intelliJ_JO.modam.domain.spend.service;

import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordResponseDto;
import com.intelliJ_JO.modam.domain.spend.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spend.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spend.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
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
public class SpendRecordService {

    private final SpendRecordRepository spendRecordRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMemberRepository accountMemberRepository;

    @Transactional
    public SpendRecordResponseDto createSpendRecord(Long memberId, SpendRecordCreateRequestDto request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다."));

        // 본인의 거래에만 소비 기록 생성 가능
        if (!transaction.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 거래에만 소비 기록을 생성할 수 있습니다.");
        }

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

    // 거래 ID로 단건 조회 — 해당 계좌의 ACCEPT 구성원만 접근 가능
    public SpendRecordResponseDto getSpendRecordByTransaction(Long memberId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다."));

        accountMemberRepository.findByAccountIdAndMemberId(transaction.getAccount().getId(), memberId)
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래에 대한 접근 권한이 없습니다."));

        SpendRecord spendRecord = spendRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래의 소비 기록을 찾을 수 없습니다."));
        return new SpendRecordResponseDto(spendRecord);
    }

    // 계좌별 소비 기록 목록 — ACCEPT 구성원만 접근 가능, 커서 기반 페이지네이션
    public List<SpendRecordResponseDto> getSpendRecordsByAccount(Long memberId, Long accountId,
                                                                  Long lastRecordId, int size) {
        accountMemberRepository.findByAccountIdAndMemberId(accountId, memberId)
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌에 대한 접근 권한이 없습니다."));

        Pageable pageable = PageRequest.of(0, size);
        List<SpendRecord> records = (lastRecordId == null)
                ? spendRecordRepository.findByTransaction_AccountIdOrderByIdDesc(accountId, pageable)
                : spendRecordRepository.findByTransaction_AccountIdAndIdLessThanOrderByIdDesc(
                        accountId, lastRecordId, pageable);

        return records.stream()
                .map(SpendRecordResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SpendRecordResponseDto updateSpendRecord(Long memberId, Long recordId, SpendRecordUpdateRequestDto request) {
        SpendRecord spendRecord = spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));

        // 본인의 거래에 대한 소비 기록만 수정 가능
        if (!spendRecord.getTransaction().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 소비 기록만 수정할 수 있습니다.");
        }

        spendRecord.updateRecord(
                request.getImageUrl() != null ? request.getImageUrl() : spendRecord.getImageUrl(),
                request.getMemo() != null ? request.getMemo() : spendRecord.getMemo(),
                request.getEmoticon() != null ? request.getEmoticon() : spendRecord.getEmoticon()
        );

        return new SpendRecordResponseDto(spendRecord);
    }

    @Transactional
    public void deleteSpendRecord(Long memberId, Long recordId) {
        SpendRecord spendRecord = spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));

        // 본인의 거래에 대한 소비 기록만 삭제 가능
        if (!spendRecord.getTransaction().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 소비 기록만 삭제할 수 있습니다.");
        }

        spendRecordRepository.delete(spendRecord);
    }
}
