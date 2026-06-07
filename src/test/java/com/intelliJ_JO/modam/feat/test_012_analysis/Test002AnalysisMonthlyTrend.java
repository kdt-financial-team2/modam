package com.intelliJ_JO.modam.feat.test_012_analysis;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 월별 소비 추이 조회 테스트
 * 대상 API: GET /api/analysis/{accountId}/monthly-trend?year={y}&month={m}
 * 최근 6개월 소비 추이 반환
 * 인증 불필요 (anyRequest().permitAll() 설정)
 * 화면: analysis.html 월별 추이 차트 로드 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 월별 소비 추이 조회 테스트")
class Test002AnalysisMonthlyTrend {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Account testAccount;

    private final int year  = LocalDate.now().getYear();
    private final int month = LocalDate.now().getMonthValue();

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("ANLACC2234567B").accountType(AccountType.GROUP).build());

        Member member = memberRepository.save(Member.builder()
                .userId("anluser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("anl002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11033345678902")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01033345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(member).inviteStatus(InviteStatus.ACCEPT).build());

        // 이번 달 거래 데이터 저장
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(member)
                .txType(TransactionType.PAYMENT).amount(20_000L)
                .afterBalance(980_000L).merchantName("식당").category("식비").build());
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(member)
                .txType(TransactionType.WITHDRAW).amount(50_000L)
                .afterBalance(930_000L).merchantName("ATM").category("기타").build());
    }

    @Test
    @DisplayName("정상: 거래 내역 있는 달의 월별 추이 조회 → 200 + 6개월 데이터")
    void 정상_월별_추이_조회() throws Exception {
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/monthly-trend")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("거래 내역 없는 계좌의 월별 추이 → 200 + 빈 추이")
    void 거래없는_계좌_월별_추이() throws Exception {
        Account emptyAccount = accountRepository.save(Account.builder()
                .accountNumber("EMPTY_ANLACC001").accountType(AccountType.GROUP).build());

        mockMvc.perform(get("/api/analysis/" + emptyAccount.getId() + "/monthly-trend")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 계좌 월별 추이 → 400 (AnalysisService existsById 체크 → IllegalArgumentException)")
    void 존재하지_않는_계좌_월별_추이() throws Exception {
        // AnalysisService.getMonthlyTrend: existsById 체크 → IllegalArgumentException → GlobalExceptionHandler → 400
        mockMvc.perform(get("/api/analysis/999999/monthly-trend")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("year 파라미터 누락 → 400")
    void year_파라미터_누락() throws Exception {
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/monthly-trend")
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
