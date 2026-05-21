package com.intelliJ_JO.modam.domain.shop.dto;

import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private String category;
    private Integer pointCost;
    private String imageUrl;
    private String description;
    private String image;

    public static ProductDto from(ItemEntity item) {
        return ProductDto.builder()
                .id(item.getId())
                .name(item.getItemName())
                .category(item.getItemType())
                .pointCost(item.getPrice())
                .imageUrl(item.getImgUrl())
                .description(item.getDescription())
                .image(item.getImage())
                .build();
    }
}
