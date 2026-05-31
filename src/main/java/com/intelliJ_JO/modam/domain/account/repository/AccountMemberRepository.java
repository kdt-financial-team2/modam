package com.intelliJ_JO.modam.domain.account.repository;

import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountMemberRepository extends JpaRepository<AccountMember, Long> {
    List<AccountMember> findByAccountId(Long accountId);
    List<AccountMember> findByMemberId(Long memberId);
    Optional<AccountMember> findFirstByMemberId(Long memberId);
    Optional<AccountMember> findByAccountIdAndMemberId(Long accountId, Long memberId);
    long countByAccountIdAndInviteStatusNot(Long accountId, InviteStatus inviteStatus);
    List<AccountMember> findByAccountIdAndInviteStatus(Long accountId, InviteStatus inviteStatus);
}