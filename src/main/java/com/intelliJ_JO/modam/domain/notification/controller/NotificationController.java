package com.intelliJ_JO.modam.domain.notification.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.notification.dto.response.NotificationResponseDto;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/subscribe — SSE 실시간 연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.subscribe(userDetails.getMember().getId());
    }

    // GET /api/notifications — 알림 목록 (커서 기반 페이지네이션)
    @GetMapping
    public GlobalResponse<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return GlobalResponse.ok(notificationService.getNotifications(
                userDetails.getMember().getId(), lastId, size));
    }

    // PATCH /api/notifications/{id}/read — 단건 읽음 처리
    @PatchMapping("/{id}/read")
    public GlobalResponse<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return GlobalResponse.ok(notificationService.markAsRead(id, userDetails.getMember().getId()));
    }

    // PATCH /api/notifications/read-all — 전체 읽음 처리
    @PatchMapping("/read-all")
    public GlobalResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getMember().getId());
        return GlobalResponse.ok("모든 알림을 읽음 처리했습니다.");
    }

    // GET /api/notifications/unread-count — 읽지 않은 알림 수
    @GetMapping("/unread-count")
    public GlobalResponse<Long> countUnread(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(notificationService.countUnread(userDetails.getMember().getId()));
    }
}
