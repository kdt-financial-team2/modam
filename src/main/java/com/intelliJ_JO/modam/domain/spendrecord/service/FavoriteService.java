package com.intelliJ_JO.modam.domain.spendrecord.service;

import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import com.intelliJ_JO.modam.domain.spendrecord.entity.Favorite;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.FavoriteRepository;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final SpendRecordRepository spendRecordRepository;
    private final NotificationService notificationService;
    private final AccountMemberRepository accountMemberRepository;

    /**
     * 즐겨찾기 토글 — 없으면 추가(true), 있으면 삭제(false) 반환
     */
    @Transactional
    public boolean toggle(Long memberId, Long recordId) {
        Optional<Favorite> existing = favoriteRepository.findByMemberIdAndSpendRecordId(memberId, recordId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        SpendRecord record = spendRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("소비 기록을 찾을 수 없습니다."));
        favoriteRepository.save(Favorite.builder()
                .member(member)
                .spendRecord(record)
                .build());

        // 공동 계좌 전원에게 즐겨찾기 알림 발송
        String recordTitle = record.getTitle() != null ? record.getTitle()
                : (record.getTransaction().getMerchantName() != null
                    ? record.getTransaction().getMerchantName() : "소비 스토리");
        String msg = String.format("%s님이 소비 스토리를 즐겨찾기했습니다: %s", member.getName(), recordTitle);
        Long accountId = record.getTransaction().getAccount().getId();
        accountMemberRepository.findByAccountId(accountId).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .forEach(am -> notificationService.send(
                        am.getMember(), NotificationType.FAVORITE, msg, "/consumption-history"));

        return true;
    }

    /**
     * 해당 회원이 즐겨찾기한 SpendRecord id 집합 반환 (뷰 렌더링용)
     */
    public Set<Long> getFavoriteRecordIds(Long memberId) {
        return favoriteRepository.findRecordIdsByMemberId(memberId);
    }

    /**
     * 커플 계좌 기준 즐겨찾기 SpendRecord id 집합 반환 — 파트너가 즐겨찾기한 스토리도 포함
     */
    public Set<Long> getFavoriteRecordIdsByAccount(Long accountId) {
        return favoriteRepository.findRecordIdsByAccountId(accountId);
    }
}
