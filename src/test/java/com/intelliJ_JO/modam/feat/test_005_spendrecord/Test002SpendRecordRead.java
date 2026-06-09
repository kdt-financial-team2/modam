package com.intelliJ_JO.modam.feat.test_005_spendrecord;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * Test-002 | 소비 기록 조회 테스트 (보조 기능)
 * 대상 API:
 *   GET /api/spend-records/transaction/{transactionId}  (거래별 단건 조회)
 *   GET /api/spend-records/account/{accountId}          (계좌별 목록 조회, 커서 기반 페이지)
 * 소비 기록 목록(consumption-history), 상세(consumption-detail) 화면 로드 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 소비 기록 조회 테스트")
class Test002SpendRecordRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL        = "/login";
    private static final String SPEND_RECORD_URL = "/api/spend-records";

    private Member testMember;
    private Account testAccount;
    private Transaction testTransaction;
    private SpendRecord testSpendRecord;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("sruser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("sr002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11055345678902")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01055345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SRACC2A34567002")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        testTransaction = transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").category("식비")
                .build());

        testSpendRecord = spendRecordRepository.save(SpendRecord.builder()
                .transaction(testTransaction)
                .title("스벅 커피")
                .memo("맛있었다")
                .emoticon("☕")
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "sruser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 거래 ID로 소비 기록 단건 조회 → 200 + merchantName 반환")
    void 정상_거래별_소비기록_단건_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // SpendRecordResponseDto 에는 title 필드 없음 — Transaction.merchantName 으로 확인
        mockMvc.perform(get(SPEND_RECORD_URL + "/transaction/" + testTransaction.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantName").value("스타벅스"));
    }

    @Test
    @DisplayName("정상: 계좌 ID로 소비 기록 목록 조회 → 200 + 배열 반환")
    void 정상_계좌별_소비기록_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(SPEND_RECORD_URL + "/account/" + testAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("소비 기록 없는 거래 조회 → 400 (IllegalArgumentException → GlobalExceptionHandler)")
    void 소비기록_없는_거래_조회() throws Exception {
        Transaction emptyTx = transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.DEPOSIT).amount(100_000L)
                .afterBalance(1_085_000L).merchantName("외부 입금").build());

        MockHttpSession session = loginAndGetSession();

        // SpendRecordService: spendRecordRepository.findByTransactionId(id)
        //   .orElseThrow(IllegalArgumentException) → GlobalExceptionHandler → 400
        mockMvc.perform(get(SPEND_RECORD_URL + "/transaction/" + emptyTx.getId()).session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션: lastId 파라미터 적용")
    void 커서_기반_페이지네이션() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(SPEND_RECORD_URL + "/account/" + testAccount.getId())
                        .param("lastId", "999999")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 소비 기록 조회 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_소비기록_조회_리다이렉트() {
        // SpendRecordController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        assertThrows(Exception.class, () ->
            mockMvc.perform(get(SPEND_RECORD_URL + "/transaction/" + testTransaction.getId())).andReturn()
        );
    }
}
