package com.intelliJ_JO.modam.domain.member.repository;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNo(String phoneNo);
}
