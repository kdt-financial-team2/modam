package com.intelliJ_JO.modam.domain.invite.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.invite.dto.InviteRequestDto;
import com.intelliJ_JO.modam.domain.invite.dto.InviteResponseDto;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 모임 통장 초대 관련 비즈니스 로직 담당 서비스
// 초대(invite) / 수락(accept) 처리를 Account 도메인에서 분리해 관리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteService {

    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // 모임 통장에 파트너를 초대 (GROUP 계좌 전용, 최대 2명)
    // inviteStatus 기본값은 AccountMember 엔티티의 @Builder.Default → WAIT
    @Transactional
    public InviteResponseDto invite(Long accountId, InviteRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        // GROUP 계좌에만 초대 가능
        if (account.getAccountType() != AccountType.GROUP) {
            throw new IllegalStateException("개인 계좌에는 회원을 초대할 수 없습니다.");
        }

        // REJECT 상태는 탈락 처리로 보기 때문에 인원 카운트에서 제외
        long activeCount = accountMemberRepository
                .countByAccountIdAndInviteStatusNot(accountId, InviteStatus.REJECT);
        if (activeCount >= 2) {
            throw new IllegalStateException("모임 통장에는 최대 2명까지 참여할 수 있습니다.");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // REJECT된 이력이 있는 회원은 재초대 허용, 그 외 중복 초대 방지
        accountMemberRepository.findByAccountIdAndMemberId(accountId, request.getMemberId())
                .filter(am -> am.getInviteStatus() != InviteStatus.REJECT)
                .ifPresent(am -> { throw new IllegalStateException("이미 초대된 회원입니다."); });

        AccountMember accountMember = AccountMember.builder()
                .account(account)
                .member(member)
                .build();

        InviteResponseDto response = new InviteResponseDto(accountMemberRepository.save(accountMember));

        String msg = String.format("계좌(%s) 모임통장에 초대되었습니다.", account.getAccountNumber());
        notificationService.send(member, NotificationType.INVITE, msg, "/invites/" + response.getId());
        return response;
    }

    // 초대 수락 처리 → inviteStatus를 ACCEPT로 변경
    @Transactional
    public InviteResponseDto accept(Long accountMemberId) {
        AccountMember accountMember = accountMemberRepository.findById(accountMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 초대 정보입니다."));

        if (accountMember.getInviteStatus() != InviteStatus.WAIT) {
            throw new IllegalStateException("대기 중인 초대만 수락할 수 있습니다.");
        }

        accountMember.acceptInvite();
        return new InviteResponseDto(accountMember);
    }
}
