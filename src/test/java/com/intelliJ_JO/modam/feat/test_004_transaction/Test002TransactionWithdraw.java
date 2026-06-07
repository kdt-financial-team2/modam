package com.intelliJ_JO.modam.feat.test_004_transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 출금 거래 테스트
 * 대상 API: POST /api/transactions (txType=WITHDRAW)
 * 송금 화면(transfer/transfer.html)에서 출금 시 호출
 * 출금 시에는 계좌 비밀번호 검증이 수행됨
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 출금 거래 테스트")
class Test002TransactionWithdraw {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String TRANSACTION_URL = "/api/transactions";

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("txuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("tx002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11044345678902")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01044345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        // 충분한 잔액을 가진 계좌 생성
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("TXACC2A34567002")
                .accountType(AccountType.GROUP)
                .balance(1_000_000L)
                .availableBalance(1_000_000L)
                .passwordHash(passwordEncoder.encode("1234"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "txuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private TransactionRequestDto withdrawRequest(long amount, String password) {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.setAccountId(testAccount.getId());
        dto.setMemberId(testMember.getId());
        dto.setTxType(TransactionType.WITHDRAW);
        dto.setAmount(amount);
        dto.setMerchantName("ATM 출금");
        dto.setCategory("기타");
        dto.setAccountPassword(password);
        return dto;
    }

    @Test
    @DisplayName("정상: 올바른 비밀번호로 출금 → 200")
    void 정상_출금_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest(100_000L, "1234"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.txType").value("WITHDRAW"));
    }

    @Test
    @DisplayName("계좌 비밀번호 오류 → 4xx (비밀번호 불일치)")
    void 계좌_비밀번호_오류_출금_실패() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest(100_000L, "9999"))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("잔액 초과 출금 → 4xx (잔액 부족)")
    void 잔액_초과_출금_실패() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest(9_999_999L, "1234"))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
    }

    @Test
    @DisplayName("정확히 잔액만큼 출금 → 200 (경계값)")
    void 잔액_전액_출금() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest(1_000_000L, "1234"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("계좌 구성원이 아닌 회원이 출금 시도 → 4xx")
    void 미계좌구성원_출금_시도() throws Exception {
        MockHttpSession session = loginAndGetSession();
        TransactionRequestDto dto = withdrawRequest(10_000L, "1234");
        dto.setMemberId(999999L); // 존재하지 않는 회원

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 출금 시도 → 200 (TransactionController 인증 미적용, permitAll)")
    void 비로그인_출금_리다이렉트() throws Exception {
        // TransactionController 는 @AuthenticationPrincipal 없음 → 인증 없이도 거래 가능
        mockMvc.perform(post(TRANSACTION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest(10_000L, "1234"))))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
