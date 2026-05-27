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
                item("봄 테마",        "theme",    500,  "🌸"),
                item("여름 테마",      "theme",    600,  "☀️"),
                item("가을 테마",      "theme",    700,  "🍂"),
                item("겨울 테마",      "theme",    800,  "❄️"),
                item("귀여운 이모티콘", "emoticon", 300, "🐰"),
                item("사랑 이모티콘",   "emoticon", 400, "💕"),
                item("웃음 이모티콘",   "emoticon", 350, "😄")
        ));
    }

    private ItemEntity item(String name, String type, int price, String image) {
        return ItemEntity.builder()
                .itemName(name)
                .itemType(type)
                .price(price)
                .image(image)
                .isActive(ItemStatus.ACTIVE)
                .build();
    }
}
