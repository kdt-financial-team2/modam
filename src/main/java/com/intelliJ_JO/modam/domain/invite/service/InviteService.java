package com.intelliJ_JO.modam.domain.invite.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.invite.dto.InviteRequestDto;
import com.intelliJ_JO.modam.domain.invite.dto.InviteResponseDto;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final NotificationService notificationService;
    private final CoupleRepository coupleRepository; // [추가] 커플(초대코드) 저장용 레포지토리

    // [기존 로직 유지] 앱 내 직접 초대
    @Transactional
    public InviteResponseDto invite(Long accountId, InviteRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

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

    // [기존 로직 유지] 앱 내 직접 초대 수락
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

    // =========================================================================
    // [신규 로직 추가] 카톡 공유용 랜덤 초대 코드 생성 및 반환
    // =========================================================================
    @Transactional
    public String generateInviteCode(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        // 이미 생성된 초대 코드가 있다면 기존 코드 반환, 없으면 새로 생성
        return coupleRepository.findByAccountId(accountId)
                .map(Couple::getInviteCode)
                .orElseGet(() -> {
                    String newCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    Couple newCouple = Couple.builder()
                            .account(account)
                            .inviteCode(newCode)
                            .build();
                    coupleRepository.save(newCouple);
                    return newCode;
                });
    }
}