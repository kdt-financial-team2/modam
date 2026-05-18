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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    // =========================================
    // 포인트 내역 전체 조회
    // =========================================
    public List<PointResponse> getPointHistories(Long memberId) {

        List<PointHistory> pointHistories =
                pointRepository.findByMemberId(memberId);

        return pointHistories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================
    // 포인트 적립
    // =========================================
    public PointResponse savePoint(PointSaveRequest request) {

        // =========================================
        // 회원 조회
        // =========================================
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() ->
                        new IllegalArgumentException("회원이 존재하지 않습니다."));

        // =========================================
        // 현재 최신 포인트 조회
        // =========================================
        int currentBalance = pointRepository
                .findTopByMemberIdOrderByCreatedAtDesc(
                        request.getMemberId()
                )
                .map(PointHistory::getAftBal)
                .orElse(0);

        // =========================================
        // 적립 후 잔액 계산
        // =========================================
        int afterBalance =
                currentBalance + request.getAmt();

        // =========================================
        // 🔥 출석 체크 하루 1회 제한
        //
        // ATTENDANCE 는
        // 하루에 한 번만 지급 가능
        // =========================================
        if (request.getReason() == PointReason.ATTENDANCE) {

            // =========================================
            // 오늘 날짜 범위 계산
            // =========================================
            LocalDate today = LocalDate.now();

            LocalDateTime startDate =
                    today.atStartOfDay();

            LocalDateTime endDate =
                    today.atTime(LocalTime.MAX);

            // =========================================
            // 오늘 이미 출석 보상 받았는지 검사
            // =========================================
            boolean alreadyAttendance =
                    pointRepository
                            .existsByMemberIdAndReasonAndCreatedAtBetween(
                                    request.getMemberId(),
                                    PointReason.ATTENDANCE,
                                    startDate,
                                    endDate
                            );

            // =========================================
            // 이미 지급된 경우 예외 처리
            // =========================================
            if (alreadyAttendance) {

                throw new IllegalStateException(
                        "오늘 이미 출석 포인트를 지급받았습니다."
                );
            }
        }

        // =========================================
        // 🔥 초대 보상 1회 제한
        // =========================================
        if (request.getReason() == PointReason.INVITE_SUCCESS) {

            boolean alreadyRewarded =
                    pointRepository.existsByMemberIdAndReason(
                            request.getMemberId(),
                            PointReason.INVITE_SUCCESS
                    );

            if (alreadyRewarded) {

                throw new IllegalStateException(
                        "이미 초대 보상을 지급받았습니다."
                );
            }
        }

        // =========================================
        // PointHistory 생성
        // =========================================
        PointHistory pointHistory =
                PointHistory.builder()
                        .member(member)
                        .type(PointType.SAVE)
                        .reason(request.getReason())
                        .amt(request.getAmt())
                        .aftBal(afterBalance)
                        .descrip(request.getDescrip())
                        .build();

        // =========================================
        // 저장
        // =========================================
        PointHistory savedPoint =
                pointRepository.save(pointHistory);

        return toResponse(savedPoint);
    }

    // =========================================
    // 포인트 사용
    // =========================================
    public PointResponse spendPoint(PointSpendRequest request) {

        // =========================================
        // 회원 조회
        // =========================================
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() ->
                        new IllegalArgumentException("회원이 존재하지 않습니다."));

        // =========================================
        // 현재 최신 포인트 조회
        // =========================================
        int currentBalance = pointRepository
                .findTopByMemberIdOrderByCreatedAtDesc(
                        request.getMemberId()
                )
                .map(PointHistory::getAftBal)
                .orElse(0);

        // =========================================
        // 포인트 부족 검사
        // =========================================
        if (currentBalance < request.getAmt()) {

            throw new IllegalStateException(
                    "포인트가 부족합니다."
            );
        }

        // =========================================
        // 사용 후 잔액 계산
        // =========================================
        int afterBalance =
                currentBalance - request.getAmt();

        // =========================================
        // PointHistory 생성
        // =========================================
        PointHistory pointHistory =
                PointHistory.builder()
                        .member(member)
                        .type(PointType.SPEND)
                        .reason(request.getReason())
                        .amt(-request.getAmt())
                        .aftBal(afterBalance)
                        .descrip(request.getDescrip())
                        .build();

        // =========================================
        // 저장
        // =========================================
        PointHistory savedPoint =
                pointRepository.save(pointHistory);

        return toResponse(savedPoint);
    }

    // =========================================
    // 현재 보유 포인트 조회
    // =========================================
    public Integer getCurrentPoint(Long memberId) {

        return pointRepository
                .findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .map(PointHistory::getAftBal)
                .orElse(0);
    }

    // =========================================
    // Entity → Response DTO 변환
    // =========================================
    private PointResponse toResponse(
            PointHistory pointHistory
    ) {

        return PointResponse.builder()
                .id(pointHistory.getId())
                .memberId(
                        pointHistory.getMember().getId()
                )
                .type(pointHistory.getType())
                .reason(pointHistory.getReason())
                .amt(pointHistory.getAmt())
                .aftBal(pointHistory.getAftBal())
                .descrip(pointHistory.getDescrip())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}