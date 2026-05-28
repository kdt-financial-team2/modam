package com.intelliJ_JO.modam.domain.member.service;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.dto.*;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {
        // 비밀번호 확인
        if (!request.getPw().equals(request.getPwConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 중복 체크
        if (memberRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
        }

        Member member = Member.builder()
                .name(request.getName())
                .userId(request.getUserId())
                .pwHash(passwordEncoder.encode(request.getPw()))
                .email(request.getEmail())
                .agreeAge(request.isAgreeAge())
                .agreeService(request.isAgreeService())
                .agreePrivacy(request.isAgreePrivacy())
                .agreeFinance(request.isAgreeFinance())
                .notif(request.isNotif())
                .agreeThirdParty(request.isAgreeThirdParty())
                .enFirst(request.getEnFirst())
                .enLast(request.getEnLast())
                .bankName(request.getBankName())
                .persAcctNo(request.getPersAcctNo())
                .zipCode(request.getZipCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .phoneNo(request.getPhoneNo())
                .profileImg(request.getProfileImg())
                .rrn(passwordEncoder.encode(request.getRrn()))
                .build();

        Member saved = memberRepository.save(member);

        if (request.getPersAcctNo() != null && !request.getPersAcctNo().isBlank()) {
            Account account = accountRepository.save(Account.builder()
                    .accountNumber(request.getPersAcctNo())
                    .accountType(AccountType.PERSONAL)
                    .build());

            accountMemberRepository.save(AccountMember.builder()
                    .account(account)
                    .member(saved)
                    .inviteStatus(InviteStatus.ACCEPT)
                    .build());
        }

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberUpdateResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (request.getPassword() != null) {
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        String newPwHash = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword()) : null;

        member.updateInfo(
                request.getName(),
                newPwHash,
                request.getEmail(),
                request.getEnFirst(),
                request.getEnLast(),
                request.getBankName(),
                request.getPersAcctNo(),
                request.getZipCode(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getPhoneNo(),
                request.getProfileImg()
        );

        return MemberUpdateResponse.from(member);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.deactivate();
    }

    // ====================================================================
    // 🔥 [마이페이지 전용] 프로필 & 비밀번호 업데이트 로직 추가
    // ====================================================================
    @Transactional
    public void updateMyPageProfile(Long memberId, String name, String phoneNo) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.updateInfo(name, null, null, null, null, null, null, null, null, null, phoneNo, null);
    }

    @Transactional
    public void updateMyPagePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(currentPassword, member.getPwHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.updateInfo(null, passwordEncoder.encode(newPassword), null, null, null, null, null, null, null, null, null, null);
    }

    // ====================================================================
    // 조장님 작업분 (아이디/비밀번호 찾기) 유지
    // ====================================================================
    public String findUserId(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 계정이 없습니다."));
        String userId = member.getUserId();
        if (userId.length() <= 3) {
            return userId.charAt(0) + "*".repeat(userId.length() - 1);
        }
        return userId.substring(0, 3) + "*".repeat(userId.length() - 3);
    }

    public boolean verifyForPasswordReset(String userId, String email) {
        return memberRepository.findByUserIdAndEmail(userId, email).isPresent();
    }

    @Transactional
    public void resetPassword(String userId, String email, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        Member member = memberRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 요청입니다."));
        member.updateInfo(null, passwordEncoder.encode(newPassword), null,
                null, null, null, null, null, null, null, null, null);
    }
}