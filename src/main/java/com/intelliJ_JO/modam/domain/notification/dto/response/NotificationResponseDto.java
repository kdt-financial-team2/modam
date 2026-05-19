package com.intelliJ_JO.modam.domain.notification.dto.response;

import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {

    private final Long id;
    private final NotificationType notiType;
    private final String message;
    private final String targetUrl;
    private final String isRead;
    private final LocalDateTime createdAt;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.notiType = notification.getNotiType();
        this.message = notification.getMessage();
        this.targetUrl = notification.getTargetUrl();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }
}
