package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.dto.response.NotificationResponseDto;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 전체 알림 목록 페이지 뷰 컨트롤러
 * - GET /notifications : 최근 알림 50건 조회 후 목록 페이지 렌더링
 */
@Controller
@RequiredArgsConstructor
public class NotificationViewController {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;
    private final DashboardService dashboardService;

    @GetMapping("/notifications")
    public String notificationsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 헤더에 필요한 포인트·알림 데이터 주입
        dashboardService.populateHeader(member, model);

        // 알림 목록 (최신 50건, 커서 없이 첫 페이지)
        List<NotificationResponseDto> notifications =
                notificationService.getNotifications(member.getId(), null, 50);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.countUnread(member.getId()));
        return "domain/notifications/list";
    }
}
