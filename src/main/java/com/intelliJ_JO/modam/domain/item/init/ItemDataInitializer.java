package com.intelliJ_JO.modam.domain.item.init;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import com.intelliJ_JO.modam.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        if (itemRepository.count() > 0) return;

        itemRepository.saveAll(List.of(
                item("봄 테마",      "theme",    500,  null),
                item("여름 테마",    "theme",    600,  null),
                item("가을 테마",    "theme",    700,  null),
                item("겨울 테마",    "theme",    800,  null),
                item("귀여운 이모티콘", "emoticon", 300, null),
                item("사랑 이모티콘",  "emoticon", 400, null),
                item("웃음 이모티콘",  "emoticon", 350, null)
        ));
    }

    private ItemEntity item(String name, String type, int price, String imgUrl) {
        return ItemEntity.builder()
                .itemName(name)
                .itemType(type)
                .price(price)
                .imgUrl(imgUrl)
                .isActive(ItemStatus.ACTIVE)
                .build();
    }
}
