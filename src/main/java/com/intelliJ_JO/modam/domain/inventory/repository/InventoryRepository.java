package com.intelliJ_JO.modam.domain.inventory.repository;

import com.intelliJ_JO.modam.domain.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    boolean existsByMemberIdAndItemId(Long memberId, Long itemId);
}
