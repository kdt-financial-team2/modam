package com.intelliJ_JO.modam.feat.test_011_invite;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 초대 수락 테스트
 * 대상 API: PATCH /api/invites/{accountMemberId}/accept
 * WAIT 상태인 초대만 수락 가능
 * 인증 불필요 (anyRequest().permitAll() 설정)
 * 화면: invite-accept.html 초대 수락 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 초대 수락 테스트")
class Test002InviteAccept {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String INVITE_URL = "/api/invites";

    private AccountMember waitingInvite;
    private AccountMember acceptedInvite;
    private AccountMember rejectedInvite;

    @BeforeEach
    void setUp() {
        Account account = accountRepository.save(Account.builder()
                .accountNumber("INVACC2234567B").accountType(AccountType.GROUP).build());

        Member member1 = memberRepository.save(Member.builder()
                .userId("invitee002a")
                .pwHash(passwordEncoder.encode("password123"))
                .name("대기중").email("inv002a@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Wait").enLast("User")
                .bankName("신한은행").persAcctNo("11022345678902")
                .zipCode("06236").address("서울 1")
                .phoneNo("01022345672")
                .rrn(passwordEncoder.encode("910101123456"))
                .build());

        Member member2 = memberRepository.save(Member.builder()
                .userId("invitee002b")
                .pwHash(passwordEncoder.encode("password123"))
                .name("수락완료").email("inv002b@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Accept").enLast("User")
                .bankName("카카오뱅크").persAcctNo("33022345678902")
                .zipCode("06001").address("서울 1")
                .phoneNo("01022399902")
                .rrn(passwordEncoder.encode("920101123456"))
                .build());

        Member member3 = memberRepository.save(Member.builder()
                .userId("invitee002c")
                .pwHash(passwordEncoder.encode("password123"))
                .name("거절완료").email("inv002c@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Reject").enLast("User")
                .bankName("국민은행").persAcctNo("55022345678902")
                .zipCode("06001").address("서울 1")
                .phoneNo("01022311102")
                .rrn(passwordEncoder.encode("930101123456"))
                .build());

        // WAIT 상태 (수락 가능)
        waitingInvite = accountMemberRepository.save(AccountMember.builder()
                .account(account).member(member1).inviteStatus(InviteStatus.WAIT).build());

        // ACCEPT 상태 (재수락 불가)
        acceptedInvite = accountMemberRepository.save(AccountMember.builder()
                .account(account).member(member2).inviteStatus(InviteStatus.ACCEPT).build());

        // REJECT 상태 (수락 불가)
        rejectedInvite = accountMemberRepository.save(AccountMember.builder()
                .account(account).member(member3).inviteStatus(InviteStatus.REJECT).build());
    }

    @Test
    @DisplayName("정상: WAIT 상태 초대 수락 → 200 + ACCEPT 상태")
    void 정상_초대_수락() throws Exception {
        mockMvc.perform(patch(INVITE_URL + "/" + waitingInvite.getId() + "/accept"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이미 ACCEPT 된 초대 재수락 → 5xx (대기 중인 초대만 수락 가능)")
    void 이미_수락된_초대_재수락() throws Exception {
        mockMvc.perform(patch(INVITE_URL + "/" + acceptedInvite.getId() + "/accept"))
                .andDo(print())
                .andExpect(status().isConflict()); // IllegalStateException → GlobalExceptionHandler → 409
    }

    @Test
    @DisplayName("REJECT 상태 초대 수락 시도 → 5xx")
    void 거절된_초대_수락_시도() throws Exception {
        mockMvc.perform(patch(INVITE_URL + "/" + rejectedInvite.getId() + "/accept"))
                .andDo(print())
                .andExpect(status().isConflict()); // IllegalStateException → GlobalExceptionHandler → 409
    }

    @Test
    @DisplayName("존재하지 않는 초대 수락 → 4xx")
    void 존재하지_않는_초대_수락() throws Exception {
        mockMvc.perform(patch(INVITE_URL + "/999999/accept"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
