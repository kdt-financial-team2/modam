package com.intelliJ_JO.modam.domain.notification.dto.response;

import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Schema(description = "알림 응답 DTO")
@Getter
public class NotificationResponseDto {

    private static final Map<NotificationType, String> ICON_MAP = Map.of(
            NotificationType.DEPOSIT, "arrow-down-circle",
            NotificationType.WITHDRAW, "arrow-up-circle",
            NotificationType.LIMIT_WARNING, "alert-triangle",
            NotificationType.INVITE, "heart-handshake",
            NotificationType.SAVINGS_GOAL, "trophy",
            NotificationType.STORY_CREATED, "book-open",
            NotificationType.FAVORITE, "heart",
            NotificationType.POINT_SPEND, "gift"
    );

    private static final Map<NotificationType, String> TITLE_MAP = Map.of(
            NotificationType.DEPOSIT, "입금 알림",
            NotificationType.WITHDRAW, "출금 알림",
            NotificationType.LIMIT_WARNING, "예산 경고",
            NotificationType.INVITE, "파트너 초대",
            NotificationType.SAVINGS_GOAL, "저축 달성",
            NotificationType.STORY_CREATED, "새 소비 스토리",
            NotificationType.FAVORITE, "즐겨찾기",
            NotificationType.POINT_SPEND, "포인트 사용"
    );

    @Schema(description = "알림 ID", example = "1")
    private final Long id;

    @Schema(description = "알림 유형 (DEPOSIT / WITHDRAW / LIMIT_WARNING / INVITE / SAVINGS_GOAL / STORY_CREATED / FAVORITE / POINT_SPEND)", example = "DEPOSIT")
    private final NotificationType notiType;

    @Schema(description = "알림 메시지", example = "50,000원이 입금되었습니다.")
    private final String message;

    @Schema(description = "이동할 URL", example = "/accounts/1/transactions")
    private final String targetUrl;

    @Schema(description = "읽음 여부 (Y / N)", example = "N")
    private final String isRead;

    @Schema(description = "알림 생성일시")
    private final LocalDateTime createdAt;

    @Schema(description = "읽음 여부 (boolean)", example = "false")
    private final boolean read;

    @Schema(description = "알림 아이콘명", example = "arrow-down-circle")
    private final String icon;

    @Schema(description = "알림 제목", example = "입금 알림")
    private final String title;

    @Schema(description = "알림 상세 설명", example = "50,000원이 입금되었습니다.")
    private final String description;

    @Schema(description = "알림 시간 (MM.dd HH:mm 형식)", example = "06.11 14:30")
    private final String time;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.notiType = notification.getNotiType();
        this.message = notification.getMessage();
        this.targetUrl = notification.getTargetUrl();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();

        this.read = "Y".equals(notification.getIsRead());
        this.icon = ICON_MAP.getOrDefault(notification.getNotiType(), "bell");
        this.title = TITLE_MAP.getOrDefault(notification.getNotiType(), "알림");
        this.description = notification.getMessage();
        this.time = notification.getCreatedAt() != null
                ? notification.getCreatedAt().format(DateTimeFormatter.ofPattern("MM.dd HH:mm"))
                : "";
    }
}
