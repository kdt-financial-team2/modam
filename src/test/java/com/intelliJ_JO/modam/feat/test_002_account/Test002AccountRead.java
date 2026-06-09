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
 * Test-002 | 계좌 단건 조회 테스트
 * 대상 API: GET /api/accounts/{accountId}
 * 대시보드(home/dashboard.html)에서 계좌 정보 로드 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 계좌 단건 조회 테스트")
class Test002AccountRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL    = "/login";
    private static final String ACCOUNTS_URL = "/api/accounts";

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("accuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("acc002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11022345678902")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01022345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("READACC1234567A")
                .accountType(AccountType.GROUP)
                .balance(1_000_000L)
                .availableBalance(1_000_000L)
                .passwordHash(passwordEncoder.encode("1234"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount)
                .member(testMember)
                .inviteStatus(InviteStatus.ACCEPT)
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "accuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 내 계좌 단건 조회 → 200 + 계좌번호·잔액 반환")
    void 정상_계좌_단건_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("READACC1234567A"))
                .andExpect(jsonPath("$.data.balance").value(1_000_000));
    }

    @Test
    @DisplayName("정상: 계좌 타입이 GROUP으로 반환")
    void 계좌_타입_GROUP_확인() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountType").value("GROUP"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("존재하지 않는 계좌 ID 조회 → 4xx 오류")
    void 존재하지_않는_계좌_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(ACCOUNTS_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 계좌 조회 → 200 (AccountController 인증 미적용, permitAll)")
    void 비로그인_계좌_조회_리다이렉트() throws Exception {
        // getAccount 엔드포인트는 @AuthenticationPrincipal 없음 → 인증 없이도 조회 가능
        mockMvc.perform(get(ACCOUNTS_URL + "/" + testAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
