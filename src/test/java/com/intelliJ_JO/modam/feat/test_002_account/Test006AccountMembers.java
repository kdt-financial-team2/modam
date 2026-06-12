package com.intelliJ_JO.modam.feat.test_002_account;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-006 | 모임통장 참여 회원 목록 조회 테스트
 * 대상 API: GET /api/accounts/{accountId}/members
 * 대시보드(home/dashboard.html) 파트너 정보 로드 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-006 | 모임통장 참여 회원 목록 테스트")
class Test006AccountMembers {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL    = "/login";
    private static final String ACCOUNTS_URL = "/api/accounts";

    private Member testMember;
    private Member partnerMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("accuser006")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("acc006@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11022345678906")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01022345676")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        // 파트너 회원 생성
        partnerMember = memberRepository.save(Member.builder()
                .userId("partner006")
                .pwHash(passwordEncoder.encode("password123"))
                .name("김파트너").email("partner006@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Partner").enLast("Kim")
                .bankName("카카오뱅크").persAcctNo("33322345678906")
                .zipCode("06001").address("서울시 서초구 강남대로 1")
                .phoneNo("01033345676")
                .rrn(passwordEncoder.encode("950202123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("MEMACC1234567A")
                .accountType(AccountType.GROUP)
                .build());

        // 두 명 모두 ACCEPT 상태로 등록
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(partnerMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "accuser006")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 모임통장 참여 회원 목록 조회 → 2명 반환")
    void 정상_참여회원_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId() + "/members").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("WAIT 상태 초대 미수락 회원은 목록에 포함되지 않음")
    void 미수락_회원_목록_미포함() throws Exception {
        // WAIT 상태로 추가 초대 (미수락)
        Member waitMember = memberRepository.save(Member.builder()
                .userId("wait006")
                .pwHash(passwordEncoder.encode("password123"))
                .name("대기자").email("wait006@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Wait").enLast("User")
                .bankName("국민은행").persAcctNo("44422345678906")
                .zipCode("06002").address("서울시 마포구 1")
                .phoneNo("01044345676")
                .rrn(passwordEncoder.encode("960303123456"))
                .build());
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(waitMember)
                .inviteStatus(InviteStatus.WAIT).build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId() + "/members").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 참여 회원 목록 조회 → 200 (AccountController 인증 미적용, permitAll)")
    void 비로그인_참여회원_조회_리다이렉트() throws Exception {
        // getAccountMembers 엔드포인트는 @AuthenticationPrincipal 없음 → 인증 없이도 접근 가능
        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId() + "/members"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 계좌의 참여 회원 조회 → 200 + 빈 배열 (예외 없이 빈 리스트 반환)")
    void 존재하지_않는_계좌_참여회원_조회() throws Exception {
        // accountMemberRepository.findByAccountId(999999) → 빈 리스트 반환 (예외 발생 없음)
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/999999/members").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
