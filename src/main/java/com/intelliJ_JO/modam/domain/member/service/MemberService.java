package com.intelliJ_JO.modam.domain.member.service;

import com.intelliJ_JO.modam.domain.member.dto.*;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MemberCreateResponse createMember(MemberCreateRequest request) {
        Member member = Member.builder()
                .name(request.getName())
                .userId(request.getUserId())
                .pwHash(passwordEncoder.encode(request.getPw()))
                .email(request.getEmail())
                .agree(request.isAgree())
                .notif(request.isNotif())
                .enFirst(request.getEnFirst())
                .enLast(request.getEnLast())
                .bankName(request.getBankName())
                .persAcctNo(request.getPersAcctNo())
                .address(request.getAddress())
                .phoneNo(request.getPhoneNo())
                .profileImg(request.getProfileImg())
                .rrn(passwordEncoder.encode(request.getRrn()))
                .build();

        Member saved = memberRepository.save(member);
        return MemberCreateResponse.from(saved);
    }

    public MemberListResponse getMembers() {
        List<MemberResponse> list = memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();

        return new MemberListResponse(list);
    }

    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        return MemberResponse.from(member);
    }

    public MemberUpdateResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Member updated = Member.builder()
                .id(member.getId())
                .name(request.getName())
                .userId(member.getUserId())
                .pwHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .agree(request.isAgree())
                .notif(request.isNotif())
                .enFirst(request.getEnFirst())
                .enLast(request.getEnLast())
                .bankName(request.getBankName())
                .persAcctNo(request.getPersAcctNo())
                .address(request.getAddress())
                .phoneNo(request.getPhoneNo())
                .profileImg(request.getProfileImg())
                .rrn(passwordEncoder.encode(request.getRrn()))
                .active(member.isActive())
                .role(member.getRole())
                .build();

        Member saved = memberRepository.save(updated);
        return MemberUpdateResponse.from(saved);
    }

    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Member updated = Member.builder()
                .id(member.getId())
                .name(member.getName())
                .userId(member.getUserId())
                .pwHash(member.getPwHash())
                .email(member.getEmail())
                .agree(member.isAgree())
                .notif(member.isNotif())
                .enFirst(member.getEnFirst())
                .enLast(member.getEnLast())
                .bankName(member.getBankName())
                .persAcctNo(member.getPersAcctNo())
                .address(member.getAddress())
                .phoneNo(member.getPhoneNo())
                .profileImg(member.getProfileImg())
                .rrn(member.getRrn())
                .active(false)
                .role(member.getRole())
                .build();

        memberRepository.save(updated);
    }
}