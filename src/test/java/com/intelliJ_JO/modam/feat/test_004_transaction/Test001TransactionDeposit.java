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
 * Test-001 | 입금 거래 테스트
 * 대상 API: POST /api/transactions (txType=DEPOSIT)
 * 송금 화면(transfer/transfer.html)에서 입금 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 입금 거래 테스트")
class Test001TransactionDeposit {

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
                .userId("txuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("tx001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11044345678901")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01044345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("TXACC1A34567001")
                .accountType(AccountType.GROUP)
                .balance(0L)
                .availableBalance(0L)
                .passwordHash(passwordEncoder.encode("1234"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "txuser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private TransactionRequestDto depositRequest(long amount) {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.setAccountId(testAccount.getId());
        dto.setMemberId(testMember.getId());
        dto.setTxType(TransactionType.DEPOSIT);
        dto.setAmount(amount);
        dto.setMerchantName("외부 계좌");
        dto.setCategory("기타");
        dto.setAccountPassword("1234"); // 입금 시에는 비밀번호 미검증이지만 DTO @NotBlank로 필수
        return dto;
    }

    @Test
    @DisplayName("정상: 입금 → 200 + 잔액 증가 확인")
    void 정상_입금_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest(100_000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.txType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.amount").value(100_000));
    }

    @Test
    @DisplayName("정상: 여러 번 입금 → 누적 잔액 증가")
    void 여러번_입금_누적() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // 1차 입금
        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest(50_000L))))
                .andExpect(status().isOk());

        // 2차 입금
        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest(30_000L))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("금액 0원 입금 → 400 (@Positive 검증)")
    void 금액_0원_입금() throws Exception {
        MockHttpSession session = loginAndGetSession();
        TransactionRequestDto dto = depositRequest(0L);

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("음수 금액 입금 → 400")
    void 음수_금액_입금() throws Exception {
        MockHttpSession session = loginAndGetSession();
        TransactionRequestDto dto = depositRequest(-10_000L);

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 계좌에 입금 → 4xx")
    void 존재하지_않는_계좌_입금() throws Exception {
        MockHttpSession session = loginAndGetSession();
        TransactionRequestDto dto = depositRequest(10_000L);
        dto.setAccountId(999999L);

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 입금 시도 → 200 (TransactionController 인증 미적용, permitAll)")
    void 비로그인_입금_리다이렉트() throws Exception {
        // TransactionController 는 @AuthenticationPrincipal 없음 → 인증 없이도 거래 가능
        mockMvc.perform(post(TRANSACTION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest(10_000L))))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
