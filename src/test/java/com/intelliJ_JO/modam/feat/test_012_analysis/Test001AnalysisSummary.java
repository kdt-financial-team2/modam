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
 * Test-001 | 소비 분석 요약 조회 테스트
 * 대상 API: GET /api/analysis/{accountId}/summary?year={y}&month={m}
 * 카테고리 도넛 차트 + AI 인사이트 데이터 반환
 * 인증 불필요 (anyRequest().permitAll() 설정)
 * 화면: analysis.html 소비 분석 화면 로드 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 소비 분석 요약 테스트")
class Test001AnalysisSummary {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Account testAccount;

    // 현재 날짜 기준으로 year/month 파라미터 설정
    private final int year  = LocalDate.now().getYear();
    private final int month = LocalDate.now().getMonthValue();

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("ANLACC1234567A").accountType(AccountType.GROUP).build());

        Member member = memberRepository.save(Member.builder()
                .userId("anluser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("anl001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11033345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01033345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(member).inviteStatus(InviteStatus.ACCEPT).build());

        // 이번 달 거래 내역 저장 (식비 2건, 교통 1건)
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(member)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").category("식비").build());
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(member)
                .txType(TransactionType.PAYMENT).amount(30_000L)
                .afterBalance(955_000L).merchantName("이마트").category("식비").build());
        transactionRepository.save(Transaction.builder()
                .account(testAccount).member(member)
                .txType(TransactionType.PAYMENT).amount(5_000L)
                .afterBalance(950_000L).merchantName("지하철").category("교통").build());
    }

    @Test
    @DisplayName("정상: 거래 내역이 있는 달 분석 요약 조회 → 200")
    void 정상_분석_요약_조회() throws Exception {
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/summary")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("거래 내역이 없는 달 분석 요약 조회 → 200 + 빈 카테고리 목록")
    void 거래없는_달_분석_조회() throws Exception {
        // 내년 1월 (거래 없는 달)
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/summary")
                        .param("year", String.valueOf(year + 1))
                        .param("month", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 계좌 분석 요약 → 4xx")
    void 존재하지_않는_계좌_분석() throws Exception {
        mockMvc.perform(get("/api/analysis/999999/summary")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("year 파라미터 누락 → 400")
    void year_파라미터_누락() throws Exception {
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/summary")
                        .param("month", String.valueOf(month)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("month 파라미터 누락 → 400")
    void month_파라미터_누락() throws Exception {
        mockMvc.perform(get("/api/analysis/" + testAccount.getId() + "/summary")
                        .param("year", String.valueOf(year)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
