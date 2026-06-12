package com.intelliJ_JO.modam.domain.inventory.repository;

import com.intelliJ_JO.modam.domain.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    boolean existsByMemberIdAndItemId(Long memberId, Long itemId);

    // 마이페이지에서 회원의 보유 아이템 목록을 가져오기 위한 메서드
    List<InventoryEntity> findByMemberId(Long memberId);

    // 소비 기록 작성 시 이모티콘 피커용 — itemType이 emoticon인 아이템만 반환 (대소문자 무관)
    @Query("SELECT i FROM InventoryEntity i WHERE i.member.id = :memberId AND LOWER(i.item.itemType) = 'emoticon'")
    List<InventoryEntity> findEmojiItemsByMemberId(@Param("memberId") Long memberId);
}