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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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

    @Transactional
    public void updateNotificationSettings(Long memberId, String deposit, String withdrawal, String weekly, String monthly) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.updateNotiSettings(deposit, withdrawal, weekly, monthly);
    }

    // 🔥 [추가됨] 크로스플랫폼 운영체제 대응 프로필 사진 서버 로컬 업로드 처리 로직
    @Transactional
    public String uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 유효하지 않습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 1. 프로젝트 최상단 루트 하위에 uploads/profiles 폴더 동적 획득 (Mac/Windows 완벽 호환)
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, "uploads", "profiles");

        // 지정 디렉토리가 없으면 디렉토리 생성 처리
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. 확장자를 추출하고 고유 파일명 랜덤 생성 (중복 차단)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String savedFilename = UUID.randomUUID().toString() + extension;

        // 3. 해당 경로로 파일 물리적 전송 및 저장
        Path targetPath = uploadPath.resolve(savedFilename);
        file.transferTo(targetPath.toFile());

        // 4. WebMvcConfig와 대응하는 리소스 웹 가상 경로 생성
        String profileImgUrl = "/uploads/profiles/" + savedFilename;

        // 5. 엔티티 정보 변경 (나머지는 null 처리하여 프로필 이미지만 갱신)
        member.updateInfo(null, null, null, null, null, null, null, null, null, null, null, profileImgUrl);

        return profileImgUrl;
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