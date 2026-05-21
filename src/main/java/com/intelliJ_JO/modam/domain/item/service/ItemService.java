package com.intelliJ_JO.modam.domain.item.service;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import com.intelliJ_JO.modam.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // DB의 itemType 값 → 화면에 표시할 한글 라벨
    private static final Map<String, String> CATEGORY_LABELS = Map.of(
            "theme",    "테마",
            "emoticon", "이모티콘"
    );

    public List<Map<String, String>> getCategories() {
        return itemRepository.findDistinctItemTypesByIsActive(ItemStatus.ACTIVE)
                .stream()
                .map(type -> Map.of(
                        "value", type,
                        "label", CATEGORY_LABELS.getOrDefault(type, type)
                ))
                .toList();
    }

    public List<ItemEntity> getItems(String category) {
        if (category == null || category.isBlank()) {
            return itemRepository.findByIsActive(ItemStatus.ACTIVE, Pageable.unpaged()).getContent();
        }
        return itemRepository.findByItemTypeAndIsActive(category, ItemStatus.ACTIVE, Pageable.unpaged()).getContent();
    }
}
