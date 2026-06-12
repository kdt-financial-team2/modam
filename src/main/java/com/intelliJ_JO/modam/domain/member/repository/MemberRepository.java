package com.intelliJ_JO.modam.domain.member.repository;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);
    List<Member> findByAccount(Account account);
    Optional<Member> findByNameAndEmail(String name, String email);
    Optional<Member> findByUserIdAndEmail(String userId, String email);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNo(String phoneNo);
    boolean existsByPersAcctNo(String persAcctNo);
}
