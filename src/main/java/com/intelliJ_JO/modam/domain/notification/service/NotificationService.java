package com.intelliJ_JO.modam.domain.notification.service;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.notification.dto.response.NotificationResponseDto;
import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    // SSE 구독 — 클라이언트당 하나의 emitter 등록, 연결 즉시 connect 이벤트 전송
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.put(memberId, emitter);
        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError(e -> emitters.remove(memberId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(memberId);
        }
        return emitter;
    }

    // 알림 저장 + SSE push — 트랜잭션 커밋 확정 후에만 SSE 발송
    // 회원이 해당 유형 알림을 꺼둔 경우(notiDeposit/notiWithdrawal = 'N')에는 저장·발송 모두 건너뜀
    @Transactional
    public void send(Member member, NotificationType type, String message, String targetUrl) {
        // 입금 알림 설정 확인
        if (type == NotificationType.DEPOSIT && "N".equals(member.getNotiDeposit())) return;
        // 출금·한도경고 알림 설정 확인 (LIMIT_WARNING도 출금 관련이므로 notiWithdrawal로 제어)
        if ((type == NotificationType.WITHDRAW || type == NotificationType.LIMIT_WARNING)
                && "N".equals(member.getNotiWithdrawal())) return;

        Notification notification = Notification.builder()
                .member(member)
                .notiType(type)
                .message(message)
                .targetUrl(targetUrl)
                .build();
        notificationRepository.save(notification);

        Long memberId = member.getId();
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 트랜잭션이 커밋된 후에만 SSE push — 롤백 시 발송 안 됨
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        pushToEmitter(memberId, notification);
                    }
                }
            );
        } else {
            pushToEmitter(memberId, notification);
        }
    }

    private void pushToEmitter(Long memberId, Notification notification) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(new NotificationResponseDto(notification)));
        } catch (IOException e) {
            emitters.remove(memberId);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long memberId, Long lastId, int size) {
        var pageable = PageRequest.of(0, size);
        List<Notification> notifications = (lastId == null)
                ? notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                : notificationRepository.findByMemberIdAndIdLessThanOrderByCreatedAtDesc(memberId, lastId, pageable);
        return notifications.stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        if (!notification.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("알림에 대한 접근 권한이 없습니다.");
        }
        notification.markAsRead();
        return new NotificationResponseDto(notification);
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.findByMemberIdAndIsRead(memberId, "N")
                .forEach(Notification::markAsRead);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long memberId) {
        return notificationRepository.countByMemberIdAndIsRead(memberId, "N");
    }
}
