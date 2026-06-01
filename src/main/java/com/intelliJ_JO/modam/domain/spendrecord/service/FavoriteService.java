package com.intelliJ_JO.modam.domain.spendrecord.service;

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

        // 내 스토리가 아닌 경우(파트너가 즐겨찾기) → 스토리 작성자에게 알림
        Member recordOwner = record.getTransaction().getMember();
        if (!recordOwner.getId().equals(memberId)) {
            String recordTitle = record.getTitle() != null ? record.getTitle()
                    : (record.getTransaction().getMerchantName() != null
                        ? record.getTransaction().getMerchantName() : "소비 스토리");
            String msg = String.format("%s님이 내 소비 스토리를 즐겨찾기했습니다: %s", member.getName(), recordTitle);
            notificationService.send(recordOwner, NotificationType.FAVORITE, msg, "/consumption-history");
        }

        return true;
    }

    /**
     * 해당 회원이 즐겨찾기한 SpendRecord id 집합 반환 (뷰 렌더링용)
     */
    public Set<Long> getFavoriteRecordIds(Long memberId) {
        return favoriteRepository.findRecordIdsByMemberId(memberId);
    }
}
