package com.intelliJ_JO.modam.domain.member.service;

import com.intelliJ_JO.modam.domain.inventory.entity.InventoryEntity;
import com.intelliJ_JO.modam.domain.inventory.enums.ApplyStatus;
import com.intelliJ_JO.modam.domain.inventory.repository.InventoryRepository;
import com.intelliJ_JO.modam.domain.item.entity.ItemEntity;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final InventoryRepository inventoryRepository;

    // ==========================================
    // 1. 테마 및 알림 설정 영역
    // ==========================================
    public String getTheme(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getTheme)
                .orElse("pink");
    }

    @Transactional
    public void updateTheme(Long memberId, String theme) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.updateTheme(theme);
    }

    // 🔥 [추가됨] 알림 설정 업데이트 로직 (옵션 A)
    @Transactional
    public void updateNotificationSettings(Long memberId, String deposit, String withdrawal, String weekly, String monthly) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.updateNotiSettings(deposit, withdrawal, weekly, monthly);
    }

    // ==========================================
    // 2. 아이템(Inventory) 조회 및 장착 영역
    // ==========================================

    @Getter
    @Builder
    public static class MyPageItemDto {
        private Long id;
        private String name;
        private String description;
        private String image;
        private String type;
        private boolean isActive;
        private String purchasedDate;

        public static MyPageItemDto from(InventoryEntity inventory) {
            ItemEntity item = inventory.getItem();
            return MyPageItemDto.builder()
                    .id(inventory.getId())
                    .name(item.getItemName())
                    .description(item.getDescription())
                    .image(item.getImage() != null ? item.getImage() : "🎁")
                    .type(item.getItemType())
                    .isActive(inventory.getApplyStatus() == ApplyStatus.APPLIED)
                    .purchasedDate(inventory.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .build();
        }
    }

    public List<MyPageItemDto> getPurchasedThemes(Long memberId) {
        return inventoryRepository.findByMemberId(memberId).stream()
                .filter(inv -> "THEME".equalsIgnoreCase(inv.getItem().getItemType()) || "theme".equalsIgnoreCase(inv.getItem().getItemType()))
                .map(MyPageItemDto::from)
                .collect(Collectors.toList());
    }

    public List<MyPageItemDto> getPurchasedEmoticons(Long memberId) {
        return inventoryRepository.findByMemberId(memberId).stream()
                .filter(inv -> "EMOTICON".equalsIgnoreCase(inv.getItem().getItemType()) || "emoticon".equalsIgnoreCase(inv.getItem().getItemType()))
                .map(MyPageItemDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void equipItem(Long memberId, Long inventoryId) {
        InventoryEntity targetInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 아이템입니다."));

        if (!targetInventory.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 아이템만 장착할 수 있습니다.");
        }

        String targetType = targetInventory.getItem().getItemType();

        List<InventoryEntity> userInventory = inventoryRepository.findByMemberId(memberId);
        userInventory.stream()
                .filter(inv -> inv.getItem().getItemType().equalsIgnoreCase(targetType))
                .filter(inv -> inv.getApplyStatus() == ApplyStatus.APPLIED)
                .forEach(inv -> inv.setApplyStatus(ApplyStatus.NOT_APPLIED));

        targetInventory.setApplyStatus(ApplyStatus.APPLIED);
    }
}