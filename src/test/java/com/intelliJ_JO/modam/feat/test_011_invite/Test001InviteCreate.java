package com.intelliJ_JO.modam.feat.test_011_invite;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 파트너 초대 생성 테스트
 * 대상 API: POST /api/invites/accounts/{accountId}
 * Request Body: { "memberId": Long }
 * 인증 불필요 (anyRequest().permitAll() 설정)
 * 화면: group-account.html 파트너 초대 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 파트너 초대 생성 테스트")
class Test001InviteCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String INVITE_BASE = "/api/invites/accounts";

    private Account testAccount;
    private Member invitee;
    private Member existingMember;

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("INVACC1234567A").accountType(AccountType.GROUP).build());

        // 초대할 신규 회원
        invitee = memberRepository.save(Member.builder()
                .userId("invitee001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("초대받는사람").email("invitee001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Invited").enLast("User")
                .bankName("신한은행").persAcctNo("11022345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01022345671")
                .rrn(passwordEncoder.encode("920101123456"))
                .build());

        // 이미 계좌에 초대된 회원 (ACCEPT 상태)
        existingMember = memberRepository.save(Member.builder()
                .userId("existing001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("기존회원").email("existing001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Exist").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33022345678901")
                .zipCode("06001").address("서울 1")
                .phoneNo("01022399901")
                .rrn(passwordEncoder.encode("880101123456"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(existingMember).inviteStatus(InviteStatus.ACCEPT).build());
    }

    @Test
    @DisplayName("정상: 신규 회원 초대 → 200 + accountMemberId 반환")
    void 정상_신규_회원_초대() throws Exception {
        Map<String, Object> body = Map.of("memberId", invitee.getId());

        mockMvc.perform(post(INVITE_BASE + "/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("이미 ACCEPT 상태인 회원 중복 초대 → 5xx (이미 초대된 회원)")
    void 이미_수락한_회원_중복_초대() throws Exception {
        Map<String, Object> body = Map.of("memberId", existingMember.getId());

        mockMvc.perform(post(INVITE_BASE + "/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("존재하지 않는 계좌로 초대 → 4xx")
    void 존재하지_않는_계좌_초대() throws Exception {
        Map<String, Object> body = Map.of("memberId", invitee.getId());

        mockMvc.perform(post(INVITE_BASE + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 회원 초대 → 4xx")
    void 존재하지_않는_회원_초대() throws Exception {
        Map<String, Object> body = Map.of("memberId", 999999L);

        mockMvc.perform(post(INVITE_BASE + "/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("memberId 누락 → 400 (@NotNull)")
    void memberId_누락() throws Exception {
        Map<String, Object> body = Map.of();

        mockMvc.perform(post(INVITE_BASE + "/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
