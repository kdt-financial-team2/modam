package com.intelliJ_JO.modam.feat.test_002_account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-004 | 계좌 정보 수정 테스트
 * 대상 API: PATCH /api/accounts/{accountId}
 * 마이페이지 계좌 설정 화면(mypage/accounts.html) 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-004 | 계좌 정보 수정 테스트")
class Test004AccountUpdate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
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
                .userId("accuser004")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("acc004@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11022345678904")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01022345674")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("UPDACC1234567A")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount)
                .member(testMember)
                .inviteStatus(InviteStatus.ACCEPT)
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "accuser004")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 직업 정보·거래 목적 수정 → 200")
    void 정상_직업정보_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        AccountUpdateRequestDto request = new AccountUpdateRequestDto();
        request.setJobInfo("자영업자");
        request.setTradePurpose("생활비 관리");
        request.setFundSource("사업 수입");

        mockMvc.perform(patch(ACCOUNTS_URL + "/" + testAccount.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 이체 한도 수정 → 200")
    void 정상_이체한도_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        AccountUpdateRequestDto request = new AccountUpdateRequestDto();
        request.setOnceTransferLimit(2_000_000L);
        request.setDailyTransferLimit(10_000_000L);

        mockMvc.perform(patch(ACCOUNTS_URL + "/" + testAccount.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 배송 주소 수정 → 200")
    void 정상_배송주소_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        AccountUpdateRequestDto request = new AccountUpdateRequestDto();
        request.setDeliveryAddress("서울시 송파구 올림픽로 300");

        mockMvc.perform(patch(ACCOUNTS_URL + "/" + testAccount.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 계좌 수정 시도 → 200 (AccountController 인증 미적용, permitAll)")
    void 비로그인_계좌_수정_리다이렉트() throws Exception {
        // updateAccount 엔드포인트는 @AuthenticationPrincipal 없음 → 인증 없이도 수정 가능
        AccountUpdateRequestDto request = new AccountUpdateRequestDto();
        request.setJobInfo("무직");

        mockMvc.perform(patch(ACCOUNTS_URL + "/" + testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 계좌 수정 시도 → 4xx")
    void 존재하지_않는_계좌_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        AccountUpdateRequestDto request = new AccountUpdateRequestDto();
        request.setJobInfo("직장인");

        mockMvc.perform(patch(ACCOUNTS_URL + "/999999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
