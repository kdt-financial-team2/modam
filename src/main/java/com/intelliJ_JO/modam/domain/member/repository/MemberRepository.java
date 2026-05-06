package com.intelliJ_JO.modam.domain.member.repository;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
