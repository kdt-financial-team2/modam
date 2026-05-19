package com.intelliJ_JO.modam.domain.point.service;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    public List<PointResponse> getPointHistories(Long memberId) {
        return pointRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PointResponse savePoint(Long memberId, PointSaveRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 비관적 락으로 현재 잔액 조회 — 동시 요청 시 Race Condition 방지
        int currentBalance = pointRepository
                .findLatestByMemberIdWithLock(memberId, PageRequest.of(0, 1))
                .stream().findFirst()
                .map(PointHistory::getAftBal)
                .orElse(0);

        if (request.getReason() == PointReason.ATTENDANCE) {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            boolean alreadyAttendance = pointRepository
                    .existsByMemberIdAndReasonAndCreatedAtBetween(
                            memberId, PointReason.ATTENDANCE, startOfDay, endOfDay);

            if (alreadyAttendance) {
                throw new IllegalStateException("오늘 이미 출석 포인트를 지급받았습니다.");
            }
        }

        if (request.getReason() == PointReason.INVITE_SUCCESS) {
            boolean alreadyRewarded = pointRepository
                    .existsByMemberIdAndReason(memberId, PointReason.INVITE_SUCCESS);

            if (alreadyRewarded) {
                throw new IllegalStateException("이미 초대 보상을 지급받았습니다.");
            }
        }

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .type(PointType.SAVE)
                .reason(request.getReason())
                .amt(request.getAmt())
                .aftBal(currentBalance + request.getAmt())
                .descrip(request.getDescrip())
                .build();

        return toResponse(pointRepository.save(pointHistory));
    }

    @Transactional
    public PointResponse spendPoint(Long memberId, PointSpendRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 비관적 락으로 현재 잔액 조회 — 동시 요청 시 Race Condition 방지
        int currentBalance = pointRepository
                .findLatestByMemberIdWithLock(memberId, PageRequest.of(0, 1))
                .stream().findFirst()
                .map(PointHistory::getAftBal)
                .orElse(0);

        if (currentBalance < request.getAmt()) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .type(PointType.SPEND)
                .reason(request.getReason())
                .amt(-request.getAmt())
                .aftBal(currentBalance - request.getAmt())
                .descrip(request.getDescrip())
                .build();

        return toResponse(pointRepository.save(pointHistory));
    }

    public Integer getCurrentPoint(Long memberId) {
        return pointRepository
                .findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .map(PointHistory::getAftBal)
                .orElse(0);
    }

    private PointResponse toResponse(PointHistory pointHistory) {
        return PointResponse.builder()
                .id(pointHistory.getId())
                .memberId(pointHistory.getMember().getId())
                .type(pointHistory.getType())
                .reason(pointHistory.getReason())
                .amt(pointHistory.getAmt())
                .aftBal(pointHistory.getAftBal())
                .descrip(pointHistory.getDescrip())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}
