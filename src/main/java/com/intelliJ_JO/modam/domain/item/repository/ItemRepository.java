package com.intelliJ_JO.modam.domain.item.repository;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    Page<ItemEntity> findByIsActive(ItemStatus isActive, Pageable pageable);

    Page<ItemEntity> findByItemTypeAndIsActive(String itemType, ItemStatus isActive, Pageable pageable);

    @Query("SELECT DISTINCT i.itemType FROM ItemEntity i WHERE i.isActive = :status ORDER BY i.itemType")
    List<String> findDistinctItemTypesByIsActive(@Param("status") ItemStatus status);
}
