package com.intelliJ_JO.modam.domain.item.repository;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @Query("SELECT DISTINCT i.itemType FROM ItemEntity i WHERE i.isActive = :status")
    List<String> findDistinctItemTypeByIsActive(ItemStatus status);

    List<ItemEntity> findByIsActive(ItemStatus status);

    List<ItemEntity> findByItemTypeAndIsActive(String itemType, ItemStatus status);
}
