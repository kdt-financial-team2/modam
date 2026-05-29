package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.repository.NotificationRepository;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import com.intelliJ_JO.modam.global.view.dto.BalanceDataItem;
import com.intelliJ_JO.modam.global.view.dto.RecentTransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AccountMemberRepository accountMemberRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsRepository savingsRepository;
    private final PointService pointService;
    private final CoupleRepository coupleRepository;
    private final NotificationRepository notificationRepository;

    // 카테고리별 차트 색상
    private static final List<String> CHART_COLORS = List.of(
            "#F5A5A5", "#A5C9E8", "#FFDAB9", "#B8E6B8", "#D8BFD8",
            "#FFD700", "#87CEEB", "#DDA0DD", "#98FB98", "#F0E68C"
    );

    // 카테고리별 Lucide 아이콘
    private static final Map<String, String> CATEGORY_ICONS = Map.of(
            "식비",     "utensils",
            "식료품",   "shopping-cart",
            "카페/음료","coffee",
            "여가",     "film",
            "교통",     "bus",
            "뷰티",     "sparkles",
            "구독",     "tv",
            "편의점",   "store",
            "현금출금", "banknote",
            "입금",     "arrow-down-circle"
    );

    public void populate(Member member, Model model) {
        // 1. 멤버의 GROUP 계좌 조회
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> "GROUP".equals(am.getAccount().getAccountType().name()))
                .findFirst()
                .orElse(null);

        if (myMembership == null) {
            populateEmpty(member, model);
            return;
        }

        Long accountId = myMembership.getAccount().getId();
        long accountBalance = myMembership.getAccount().getBalance();
        model.addAttribute("accountBalance", accountBalance);

        // 2. 이번 달 기간 설정
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd   = monthStart.plusMonths(1);
        List<TransactionType> spendTypes = List.of(TransactionType.PAYMENT, TransactionType.WITHDRAW);

        // 3. 이번 달 총 지출
        long totalExpense = transactionRepository
                .sumSpendByAccountAndPeriod(accountId, spendTypes, monthStart, monthEnd);
        model.addAttribute("totalExpense", totalExpense);

        // 4. 카테고리별 지출 (도넛 차트)
        List<Object[]> categoryRows = transactionRepository
                .sumSpendGroupByCategory(accountId, monthStart, monthEnd);
        List<BalanceDataItem> balanceData = buildBalanceData(categoryRows);
        model.addAttribute("balanceData", balanceData);

        // 5. 최근 거래 5건
        List<Transaction> recent = transactionRepository
                .findByAccountIdOrderByIdDesc(accountId, PageRequest.of(0, 5));
        model.addAttribute("recentTransactions", toRecentDtos(recent));

        // 6. 저축 목표 (첫 번째)
        List<Savings> savingsList = savingsRepository.findByAccountId(accountId);
        if (!savingsList.isEmpty()) {
            Savings s = savingsList.get(0);
            long percent = s.getTargetAmount() > 0
                    ? s.getCurrentAmount() * 100 / s.getTargetAmount() : 0;
            model.addAttribute("savingsGoalName",    s.getSaveType());
            model.addAttribute("savingsGoalTarget",  s.getTargetAmount());
            model.addAttribute("savingsGoalCurrent", s.getCurrentAmount());
            model.addAttribute("savingsGoalPercent", percent);
        } else {
            model.addAttribute("savingsGoalName",    "");
            model.addAttribute("savingsGoalTarget",  0);
            model.addAttribute("savingsGoalCurrent", 0);
            model.addAttribute("savingsGoalPercent", 0);
        }

        // 6-2. 오늘 출석 체크 여부
        model.addAttribute("isCheckedIn", pointService.isCheckedIn(member.getId()));

        // 7. 커플 포인트
        model.addAttribute("couplePoints", pointService.getCurrentPoint(member.getId()));

        // 8. 커플 정보
        coupleRepository.findByAccountId(accountId).ifPresentOrElse(couple -> {
            model.addAttribute("isCoupleInfoSaved", couple.getDDay() != null && couple.getAccountAlias() != null);
            model.addAttribute("coupleStartDate",   formatDate(couple.getDDay()));
            model.addAttribute("daysTogether",      daysTogether(couple.getDDay()));
            model.addAttribute("coupleAcctAlias",   couple.getAccountAlias() != null ? couple.getAccountAlias() : "");
        }, () -> {
            model.addAttribute("isCoupleInfoSaved", false);
            model.addAttribute("coupleStartDate",   "");
            model.addAttribute("daysTogether",      0);
            model.addAttribute("coupleAcctAlias",   "");
        });

        // 9. 파트너 연결 여부
        List<AccountMember> allMembers = accountMemberRepository.findByAccountId(accountId);
        AccountMember partner = allMembers.stream()
                .filter(am -> !am.getMember().getId().equals(member.getId()))
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .findFirst()
                .orElse(null);
        model.addAttribute("partnerConnected", partner != null);
        model.addAttribute("partnerName", partner != null ? partner.getMember().getName() : "");
        model.addAttribute("partnerProfileImg", partner != null ? partner.getMember().getProfileImg() : null);

        // 10. 알림
        List<Notification> notifications = notificationRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId(), PageRequest.of(0, 10));
        long unreadCount = notificationRepository.countByMemberIdAndIsRead(member.getId(), "N");
        model.addAttribute("notifications", toNotificationDtos(notifications));
        model.addAttribute("unreadCount",   unreadCount);
    }

    // ===== 빌더 헬퍼 =====

    private List<BalanceDataItem> buildBalanceData(List<Object[]> rows) {
        int i = 0;
        var result = new java.util.ArrayList<BalanceDataItem>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            long value = ((Number) row[1]).longValue();
            String color = CHART_COLORS.get(i % CHART_COLORS.size());
            result.add(new BalanceDataItem(category, value, color));
            i++;
        }
        return result;
    }

    private List<RecentTransactionDto> toRecentDtos(List<Transaction> transactions) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        return transactions.stream().map(tx -> new RecentTransactionDto(
                resolveIcon(tx.getCategory(), tx.getTxType()),
                tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory(),
                tx.getCreatedAt().format(dateFmt),
                tx.getCreatedAt().format(timeFmt),
                tx.getCategory() != null ? tx.getCategory() : "",
                tx.getAmount()
        )).toList();
    }

    private String resolveIcon(String category, TransactionType type) {
        if (type == TransactionType.DEPOSIT) return "arrow-down-circle";
        if (category == null) return "receipt";
        return CATEGORY_ICONS.getOrDefault(category, "receipt");
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    private long daysTogether(LocalDate dDay) {
        if (dDay == null) return 0;
        return ChronoUnit.DAYS.between(dDay, LocalDate.now());
    }

    // 알림 → 헤더가 기대하는 필드(id, title, description, time, icon, read)로 변환
    private List<Object> toNotificationDtos(List<Notification> list) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM.dd HH:mm");
        return list.stream().map(n -> new java.util.HashMap<String, Object>() {{
            put("id",          n.getId());
            put("title",       resolveNotiTitle(n.getNotiType()));
            put("description", n.getMessage());
            put("time",        n.getCreatedAt().format(fmt));
            put("icon",        resolveNotiIcon(n.getNotiType()));
            put("read",        "Y".equals(n.getIsRead()));
        }}).collect(java.util.stream.Collectors.toList());
    }

    private String resolveNotiTitle(com.intelliJ_JO.modam.domain.notification.entity.NotificationType type) {
        return switch (type) {
            case DEPOSIT       -> "입금 알림";
            case WITHDRAW      -> "출금 알림";
            case LIMIT_WARNING -> "예산 경고";
            case INVITE        -> "파트너 초대";
            case SAVINGS_GOAL  -> "저축 달성";
        };
    }

    private String resolveNotiIcon(com.intelliJ_JO.modam.domain.notification.entity.NotificationType type) {
        return switch (type) {
            case DEPOSIT       -> "arrow-down-circle";
            case WITHDRAW      -> "arrow-up-circle";
            case LIMIT_WARNING -> "alert-triangle";
            case INVITE        -> "heart-handshake";
            case SAVINGS_GOAL  -> "trophy";
        };
    }

    // 대시보드 외 페이지에서 header-dashboard에 필요한 최소 속성만 채움
    public void populateHeader(Member member, Model model) {
        model.addAttribute("couplePoints", pointService.getCurrentPoint(member.getId()));

        List<Notification> notifications = notificationRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId(), PageRequest.of(0, 10));
        long unreadCount = notificationRepository.countByMemberIdAndIsRead(member.getId(), "N");
        model.addAttribute("notifications", toNotificationDtos(notifications));
        model.addAttribute("unreadCount",   unreadCount);
    }

    private void populateEmpty(Member member, Model model) {
        model.addAttribute("accountBalance",     0);
        model.addAttribute("totalExpense",      0);
        model.addAttribute("balanceData",       List.of());
        model.addAttribute("recentTransactions",List.of());
        model.addAttribute("savingsGoalName",   "");
        model.addAttribute("savingsGoalTarget", 0);
        model.addAttribute("savingsGoalCurrent",0);
        model.addAttribute("savingsGoalPercent",0);
        model.addAttribute("couplePoints",      0);
        model.addAttribute("isCoupleInfoSaved", false);
        model.addAttribute("coupleStartDate",   "");
        model.addAttribute("daysTogether",      0);
        model.addAttribute("coupleAcctAlias",   "");
        model.addAttribute("partnerConnected",  false);
        model.addAttribute("partnerName",       "");
        model.addAttribute("partnerProfileImg", null);
        model.addAttribute("isCheckedIn",       false);
        model.addAttribute("notifications",     List.of());
        model.addAttribute("unreadCount",       0);
    }
}
