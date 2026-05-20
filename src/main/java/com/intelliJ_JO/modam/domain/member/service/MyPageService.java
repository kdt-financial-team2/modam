package com.intelliJ_JO.modam.domain.member.service;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;

    public String getTheme(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getTheme)
                .orElse("pink");
    }

    @Transactional
    public void updateTheme(Long memberId, String theme) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.updateTheme(theme);
    }
}
