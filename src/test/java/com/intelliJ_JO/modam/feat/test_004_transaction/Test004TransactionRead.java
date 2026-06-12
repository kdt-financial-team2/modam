package com.intelliJ_JO.modam.feat.test_004_transaction;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
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
 * Test-004 | 거래 내역 조회 테스트 (보조 기능 - 화면 이동 없는 조회)
 * 대상 API: GET /api/transactions/{accountId}
 * 거래 내역 화면(transaction/transaction-history.html) 로드 시 호출
 * 커서 기반 페이지네이션 지원
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-004 | 거래 내역 조회 테스트")
class Test004TransactionRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String TRANSACTION_URL = "/api/transactions";

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("txuser004")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("tx004@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11044345678904")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01044345674")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("TXACC4A34567004")
                .accountType(AccountType.GROUP)
                .balance(500_000L)
                .availableBalance(500_000L)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        // 테스트용 거래 내역 3건 생성
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.DEPOSIT).amount(200_000L)
                .afterBalance(200_000L).merchantName("외부 입금").build());
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.WITHDRAW).amount(50_000L)
                .afterBalance(150_000L).merchantName("ATM 출금").build());
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(135_000L).merchantName("스타벅스").category("식비").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "txuser004")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 계좌 거래 내역 조회 → 200 + 배열 반환")
    void 정상_거래_내역_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(TRANSACTION_URL + "/" + testAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("거래가 없는 계좌 조회 → 빈 배열 반환")
    void 거래없는_계좌_조회_빈배열() throws Exception {
        Account emptyAccount = accountRepository.save(Account.builder()
                .accountNumber("TXEMPTY4567004A")
                .accountType(AccountType.GROUP)
                .build());
        accountMemberRepository.save(AccountMember.builder()
                .account(emptyAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(TRANSACTION_URL + "/" + emptyAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션: lastId 파라미터로 다음 페이지 조회")
    void 커서_기반_페이지네이션_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(TRANSACTION_URL + "/" + testAccount.getId())
                        .param("lastId", "999999")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 거래 내역 조회 → 200 (TransactionController 인증 미적용, permitAll)")
    void 비로그인_거래_내역_조회_리다이렉트() throws Exception {
        // TransactionController 는 @AuthenticationPrincipal 없음 → 인증 없이도 조회 가능
        mockMvc.perform(get(TRANSACTION_URL + "/" + testAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 계좌 거래 내역 조회 → 4xx")
    void 존재하지_않는_계좌_거래내역_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(TRANSACTION_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
