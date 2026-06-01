package com.intelliJ_JO.modam.domain.notification.dto.response;

import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

    private final Long id;
    private final NotificationType notiType;
    private final String message;
    private final String targetUrl;
    private final String isRead;
    private final LocalDateTime createdAt;

    private final boolean read;
    private final String icon;
    private final String title;
    private final String description;
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
