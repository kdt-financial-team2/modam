package com.intelliJ_JO.modam.domain.point.service;

import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import com.intelliJ_JO.modam.domain.point.dto.response.PointResponse;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSpendRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointHistory;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import com.intelliJ_JO.modam.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final CoupleRepository coupleRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public List<PointResponse> getPointHistories(Long memberId) {
        Couple couple = getCoupleByMemberId(memberId);
        return pointRepository.findByCoupleIdOrderByCreatedAtDesc(couple.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PointResponse savePoint(Long memberId, PointSaveRequest request) {
        return savePointForCouple(getCoupleByMemberId(memberId), request);
    }

    @Transactional
    public PointResponse spendPoint(Long memberId, PointSpendRequest request) {
        Couple couple = getCoupleByMemberId(memberId);

        int currentBalance = pointRepository
                .findLatestByCoupleIdWithLock(couple.getId(), PageRequest.of(0, 1))
                .stream().findFirst()
                .map(PointHistory::getAftBal)
                .orElse(0);

        if (currentBalance < request.getAmt()) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        PointHistory pointHistory = PointHistory.builder()
                .couple(couple)
                .type(PointType.SPEND)
                .reason(request.getReason())
                .amt(-request.getAmt())
                .aftBal(currentBalance - request.getAmt())
                .descrip(request.getDescrip())
                .build();

        PointResponse result = toResponse(pointRepository.save(pointHistory));

        // 포인트 사용자에게 알림 발송
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        String msg = String.format("%,dP를 사용했습니다. (잔액: %,dP)", request.getAmt(), pointHistory.getAftBal());
        notificationService.send(member, NotificationType.POINT_SPEND, msg, "/point-shop");

        return result;
    }

    public Integer getCurrentPoint(Long memberId) {
        return findCoupleByMemberId(memberId)
                .flatMap(c -> pointRepository.findTopByCoupleIdOrderByCreatedAtDesc(c.getId()))
                .map(PointHistory::getAftBal)
                .orElse(0);
    }

    @Transactional
    public PointResponse savePointByAccountId(Long accountId, PointSaveRequest request) {
        Couple couple = coupleRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("공동 계좌가 없습니다."));
        return savePointForCouple(couple, request);
    }

    /**
     * 이번 달 1일 00:00 ~ 말일 23:59 사이에 적립된 포인트 합계를 반환합니다.
     * SAVE 타입(적립)만 집계하며, 사용(SPEND)은 포함하지 않습니다.
     */
    public int getMonthlyEarnedPoints(Long memberId) {
        return findCoupleByMemberId(memberId).<Integer>map(couple -> {
            LocalDate now = LocalDate.now();
            LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd   = monthStart.plusMonths(1);
            return pointRepository.findByCoupleIdAndCreatedAtBetween(couple.getId(), monthStart, monthEnd)
                    .stream()
                    .filter(p -> p.getType() == PointType.SAVE)
                    .mapToInt(PointHistory::getAmt)
                    .sum();
        }).orElse(0);
    }

    public boolean isCheckedIn(Long memberId) {
        return findCoupleByMemberId(memberId).map(couple -> {
            LocalDate today = LocalDate.now();
            return pointRepository.existsByCoupleIdAndReasonAndCreatedAtBetween(
                    couple.getId(), PointReason.ATTENDANCE,
                    today.atStartOfDay(), today.atTime(LocalTime.MAX));
        }).orElse(false);
    }

    private Optional<Couple> findCoupleByMemberId(Long memberId) {
        return accountMemberRepository.findByMemberId(memberId).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> am.getAccount().getAccountType() == AccountType.GROUP)
                .findFirst()
                .flatMap(am -> coupleRepository.findByAccountId(am.getAccount().getId()));
    }

    private Couple getCoupleByMemberId(Long memberId) {
        return findCoupleByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("공동 계좌가 없습니다."));
    }

    private PointResponse savePointForCouple(Couple couple, PointSaveRequest request) {
        int currentBalance = pointRepository
                .findLatestByCoupleIdWithLock(couple.getId(), PageRequest.of(0, 1))
                .stream().findFirst()
                .map(PointHistory::getAftBal)
                .orElse(0);

        if (request.getReason() == PointReason.ATTENDANCE) {
            LocalDate today = LocalDate.now();
            boolean alreadyAttendance = pointRepository
                    .existsByCoupleIdAndReasonAndCreatedAtBetween(
                            couple.getId(), PointReason.ATTENDANCE,
                            today.atStartOfDay(), today.atTime(LocalTime.MAX));
            if (alreadyAttendance) {
                throw new IllegalStateException("오늘 이미 출석 포인트를 지급받았습니다.");
            }
        }

        if (request.getReason() == PointReason.INVITE_SUCCESS) {
            if (pointRepository.existsByCoupleIdAndReason(couple.getId(), PointReason.INVITE_SUCCESS)) {
                throw new IllegalStateException("이미 초대 보상을 지급받았습니다.");
            }
        }

        PointHistory pointHistory = PointHistory.builder()
                .couple(couple)
                .type(PointType.SAVE)
                .reason(request.getReason())
                .amt(request.getAmt())
                .aftBal(currentBalance + request.getAmt())
                .descrip(request.getDescrip())
                .build();

        return toResponse(pointRepository.save(pointHistory));
    }

    private PointResponse toResponse(PointHistory pointHistory) {
        return PointResponse.builder()
                .id(pointHistory.getId())
                .coupleId(pointHistory.getCouple().getId())
                .type(pointHistory.getType())
                .reason(pointHistory.getReason())
                .amt(pointHistory.getAmt())
                .aftBal(pointHistory.getAftBal())
                .descrip(pointHistory.getDescrip())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}
