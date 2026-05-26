package com.intelliJ_JO.modam.domain.couple.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 커플 정보 관련 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class CoupleService {

    private final AccountMemberRepository accountMemberRepository;
    private final CoupleRepository coupleRepository;

    /**
     * 멤버의 커플 정보 조회 — 수정 폼 초기값 표시에 사용
     */
    @Transactional(readOnly = true)
    public Couple getCoupleByMember(Member member) {
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> am.getAccount().getAccountType() == AccountType.GROUP)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("모임 통장이 없습니다."));
        return coupleRepository.findByAccountId(myMembership.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException("커플 정보가 없습니다."));
    }

    /**
     * 커플 시작일 및 계좌 애칭 저장/수정
     * - 멤버의 승인된 GROUP 계좌를 찾아 Couple 레코드를 업데이트하거나 없으면 신규 생성
     */
    @Transactional
    public void updateCoupleInfo(Member member, LocalDate dDay, String acctAlias) {
        // 멤버가 수락(ACCEPT) 상태인 GROUP 계좌 조회
        List<AccountMember> memberships = accountMemberRepository.findByMemberId(member.getId());
        AccountMember myMembership = memberships.stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> am.getAccount().getAccountType() == AccountType.GROUP)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("모임 통장이 없습니다."));

        Account account = myMembership.getAccount();

        // Couple 레코드가 없으면 새로 생성 (계좌 개설 시 누락된 경우 대비)
        Couple couple = coupleRepository.findByAccountId(account.getId())
                .orElseGet(() -> coupleRepository.save(
                        Couple.builder()
                                .account(account)
                                .inviteCode(UUID.randomUUID().toString())
                                .build()
                ));

        couple.updateCoupleInfo(acctAlias, dDay);
    }
}
    private final CoupleRepository coupleRepository;
    private final MemberRepository memberRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final PointService pointService;

    @Transactional
    public void acceptInviteCode(Long inviteeMemberId, String inputCode) {
        // 1. 회원 검증
        Member invitee = memberRepository.findById(inviteeMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 초대 코드 검증
        Couple couple = coupleRepository.findAll().stream()
                .filter(c -> c.getInviteCode().equals(inputCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        Account account = couple.getAccount();

        // ====================================================================
        // 🔥 원석님이 짚어주신 핵심 방어 로직 추가! 🔥
        // ====================================================================

        // [방어 로직 1] GROUP 계좌에만 연결 가능
        if (account.getAccountType() != AccountType.GROUP) {
            throw new IllegalStateException("개인 계좌에는 파트너를 연결할 수 없습니다.");
        }

        // [방어 로직 2] 인원수 체크 (최대 2명) - REJECT 상태는 제외하고 카운트
        long activeCount = accountMemberRepository.countByAccountIdAndInviteStatusNot(account.getId(), InviteStatus.REJECT);
        if (activeCount >= 2) {
            throw new IllegalStateException("모임 통장에는 최대 2명까지 참여할 수 있습니다.");
        }

        // [방어 로직 3] 중복 가입 방지 (이미 수락했거나 대기 중인 경우)
        accountMemberRepository.findByAccountIdAndMemberId(account.getId(), inviteeMemberId)
                .filter(am -> am.getInviteStatus() != InviteStatus.REJECT)
                .ifPresent(am -> { throw new IllegalStateException("이미 연결된 계좌이거나 대기 중인 상태입니다."); });

        // ====================================================================

        // 3. 모임 통장 멤버로 추가 및 ACCEPT 상태 변경
        AccountMember newMember = AccountMember.builder()
                .account(account)
                .member(invitee)
                .build();
        newMember.acceptInvite();
        accountMemberRepository.save(newMember);

        // 4. 축하 포인트 지급
        PointSaveRequest pointRequest = new PointSaveRequest();
        pointRequest.setReason(PointReason.INVITE_SUCCESS);
        pointRequest.setAmt(1000);
        pointRequest.setDescrip("초대 코드를 통해 파트너와 성공적으로 연결되었습니다!");

        pointService.savePoint(inviteeMemberId, pointRequest);
    }
}
