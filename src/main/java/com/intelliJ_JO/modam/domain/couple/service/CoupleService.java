package com.intelliJ_JO.modam.domain.couple.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
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
