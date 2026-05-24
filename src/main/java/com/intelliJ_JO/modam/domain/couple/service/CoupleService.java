package com.intelliJ_JO.modam.domain.couple.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
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

@Service
@RequiredArgsConstructor
public class CoupleService {

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

        // [방어 로직] REJECT된 이력이 있는 회원은 재초대 허용, 그 외 중복 연결 방지
        accountMemberRepository.findByAccountIdAndMemberId(account.getId(), inviteeMemberId)
                .filter(am -> am.getInviteStatus() != InviteStatus.REJECT)
                .ifPresent(am -> { throw new IllegalStateException("이미 연결된 계좌이거나 대기 중인 상태입니다."); });

        // 3. 모임 통장 멤버로 추가 및 ACCEPT 상태 변경
        AccountMember newMember = AccountMember.builder()
                .account(account)
                .member(invitee)
                .build();
        newMember.acceptInvite();
        accountMemberRepository.save(newMember);

        // 4. 축하 포인트 지급
        PointSaveRequest pointRequest = new PointSaveRequest();

        // 🔥 Enum으로 처리! (PointReason.java에 정의된 이벤트/초대 관련 상수 확인 필요)
        // 만약 INVITE라는 상수가 없다면 PointReason.EVENT 등으로 변경해주세요.
        pointRequest.setReason(PointReason.INVITE_SUCCESS);
        pointRequest.setAmt(1000);
        pointRequest.setDescrip("초대 코드를 통해 파트너와 성공적으로 연결되었습니다!");

        pointService.savePoint(inviteeMemberId, pointRequest);
    }
}