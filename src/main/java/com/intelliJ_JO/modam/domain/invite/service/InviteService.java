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

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final NotificationService notificationService;
    private final CoupleRepository coupleRepository;
    private final EmailService emailService;

    @Transactional
    public InviteResponseDto invite(Long accountId, InviteRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

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
    // 🔥 [버그 픽스] 안전한 6자리 영문대문자+숫자 초대 코드 생성 로직
    // =========================================================================
    @Transactional
    public String generateInviteCode(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        return coupleRepository.findByAccountId(accountId)
                .map(couple -> {
                    // 방어 로직: 기존 DB에 저장된 코드가 6자리가 아닐 경우 (이전 버그 데이터)
                    if (couple.getInviteCode() == null || couple.getInviteCode().length() != 6) {
                        String newFixedCode = createRandomCode();
                        couple.updateInviteCode(newFixedCode); // 더티 체킹을 통해 DB 업데이트
                        return newFixedCode;
                    }
                    return couple.getInviteCode();
                })
                .orElseGet(() -> {
                    String newCode = createRandomCode();
                    Couple newCouple = Couple.builder()
                            .account(account)
                            .inviteCode(newCode)
                            .build();
                    coupleRepository.save(newCouple);
                    return newCode;
                });
    }

    @Transactional
    public void sendInviteCodeByEmail(Long accountId, String email) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        String inviteCode = generateInviteCode(accountId);
        emailService.sendInviteCode(email, account.getAccountNumber(), inviteCode);
    }

    // 보안이 강화된(SecureRandom) 6자리 난수 생성기 내부 메서드
    private String createRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}