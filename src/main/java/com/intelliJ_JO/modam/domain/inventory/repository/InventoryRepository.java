package com.intelliJ_JO.modam.domain.inventory.repository;

import com.intelliJ_JO.modam.domain.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    boolean existsByMemberIdAndItemId(Long memberId, Long itemId);

    // 🔥 [추가됨] 마이페이지에서 회원의 보유 아이템 목록을 가져오기 위한 메서드
    List<InventoryEntity> findByMemberId(Long memberId);
}