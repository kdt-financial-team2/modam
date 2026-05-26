package com.intelliJ_JO.modam.domain.shop.service;

import com.intelliJ_JO.modam.domain.inventory.entity.InventoryEntity;
import com.intelliJ_JO.modam.domain.inventory.enums.ApplyStatus;
import com.intelliJ_JO.modam.domain.inventory.repository.InventoryRepository;
import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.item.enums.ItemStatus;
import com.intelliJ_JO.modam.domain.item.repository.ItemRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSpendRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.repository.PointRepository;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.domain.shop.dto.CategoryDto;
import com.intelliJ_JO.modam.domain.shop.dto.HistoryItemDto;
import com.intelliJ_JO.modam.domain.shop.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private static final int PRODUCT_PAGE_SIZE = 12;
    private static final int HISTORY_PAGE_SIZE = 10;

    private static final Map<String, String> CATEGORY_LABELS = Map.of(
            "THEME",  "테마",
            "EMOJI",  "이모지",
            "COUPON", "쿠폰",
            "GIFT",   "선물"
    );

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final PointService pointService;
    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    public Page<ProductDto> getProducts(String category, int page) {
        Pageable pageable = PageRequest.of(page - 1, PRODUCT_PAGE_SIZE);
        if (category != null && !category.isBlank()) {
            return itemRepository.findByItemTypeAndIsActive(category, ItemStatus.ACTIVE, pageable)
                    .map(ProductDto::from);
        }
        return itemRepository.findByIsActive(ItemStatus.ACTIVE, pageable).map(ProductDto::from);
    }

    public List<CategoryDto> getCategories() {
        return itemRepository.findDistinctItemTypesByIsActive(ItemStatus.ACTIVE)
                .stream()
                .map(type -> new CategoryDto(type, CATEGORY_LABELS.getOrDefault(type, type)))
                .collect(Collectors.toList());
    }

    public ProductDto getProduct(Long id) {
        return ProductDto.from(itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다.")));
    }

    public Page<HistoryItemDto> getHistory(Long memberId, int page) {
        Pageable pageable = PageRequest.of(page - 1, HISTORY_PAGE_SIZE);
        return pointRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(HistoryItemDto::from);
    }

    @Transactional
    public int purchaseItem(Long memberId, Long itemId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (inventoryRepository.existsByMemberIdAndItemId(memberId, itemId)) {
            throw new IllegalStateException("이미 보유한 상품입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        PointReason reason = "THEME".equals(item.getItemType())
                ? PointReason.THEME_PURCHASE
                : PointReason.ITEM_PURCHASE;

        PointSpendRequest spendRequest = PointSpendRequest.builder()
                .reason(reason)
                .amt(item.getPrice())
                .descrip(item.getItemName() + " 구매")
                .build();

        int remainingPoints = pointService.spendPoint(memberId, spendRequest).getAftBal();

        inventoryRepository.save(InventoryEntity.builder()
                .member(member)
                .item(item)
                .applyStatus(ApplyStatus.NOT_APPLIED)
                .build());

        return remainingPoints;
    }
}
