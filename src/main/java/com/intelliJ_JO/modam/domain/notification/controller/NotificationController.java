package com.intelliJ_JO.modam.domain.notification.controller;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.notification.dto.response.NotificationResponseDto;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "알림", description = "SSE 실시간 알림 구독/조회/읽음 처리 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "SSE 알림 구독")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.subscribe(userDetails.getMember().getId());
    }

    @Operation(summary = "알림 목록 조회 (커서 기반 페이지네이션)")
    @GetMapping
    public GlobalResponse<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return GlobalResponse.ok(notificationService.getNotifications(
                userDetails.getMember().getId(), lastId, size));
    }

    @Operation(summary = "알림 단건 읽음 처리")
    @PatchMapping("/{id}/read")
    public GlobalResponse<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return GlobalResponse.ok(notificationService.markAsRead(id, userDetails.getMember().getId()));
    }

    @Operation(summary = "전체 알림 읽음 처리")
    @PatchMapping("/read-all")
    public GlobalResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getMember().getId());
        return GlobalResponse.ok("모든 알림을 읽음 처리했습니다.");
    }

    @Operation(summary = "읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    public GlobalResponse<Long> countUnread(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponse.ok(notificationService.countUnread(userDetails.getMember().getId()));
    }
}
