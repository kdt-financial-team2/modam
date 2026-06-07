package com.intelliJ_JO.modam.feat.test_007_savings;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 저축 목표 납입 테스트
 * 대상 API: PATCH /api/savings/{savingsId}/deposit?memberId={id}
 * Request Body: Long (납입 금액, 단순 숫자 JSON)
 * 인증 불필요 (anyRequest().permitAll())
 *
 * 주의: checkAndAwardPoints()는 목표 50%/100% 달성 시 Couple 엔티티를 조회함.
 *       기본 납입 테스트는 목표 금액에 도달하지 않는 소액을 사용해 Couple 의존성을 회피.
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 저축 납입 테스트")
class Test003SavingsDeposit {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SavingsRepository savingsRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Member testMember;
    private Savings testSavings;
    private Account testAccount;

    private static final String SAVINGS_URL = "/api/savings";

    @BeforeEach
    void setUp() {
        // 계좌 잔액 1,000,000원 보유
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SAVACC3234567C").accountType(AccountType.GROUP)
                .balance(1_000_000L).availableBalance(1_000_000L).build());

        testMember = memberRepository.save(Member.builder()
                .userId("savuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("sav003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11077345678903")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01077345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(testAccount)
                .build());

        // 목표 금액 100만원 (납입은 소액만 테스트해 포인트 지급 트리거 방지)
        testSavings = savingsRepository.save(Savings.builder()
                .account(testAccount).goalName("여행 자금").saveType("여행")
                .targetAmount(1_000_000L).targetDate(LocalDate.now().plusYears(1)).build());
    }

    @Test
    @DisplayName("정상: 소액 납입 (포인트 트리거 없음) → 200")
    void 정상_소액_납입() throws Exception {
        mockMvc.perform(patch(SAVINGS_URL + "/" + testSavings.getId() + "/deposit")
                        .param("memberId", testMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("납입 금액 0 → 400 (0원보다 커야 함)")
    void 납입금액_0원() throws Exception {
        mockMvc.perform(patch(SAVINGS_URL + "/" + testSavings.getId() + "/deposit")
                        .param("memberId", testMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("납입 금액 음수 → 400")
    void 납입금액_음수() throws Exception {
        mockMvc.perform(patch(SAVINGS_URL + "/" + testSavings.getId() + "/deposit")
                        .param("memberId", testMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-5000"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("계좌 잔액 초과 납입 → 4xx/5xx (잔액 부족)")
    void 잔액_초과_납입() throws Exception {
        mockMvc.perform(patch(SAVINGS_URL + "/" + testSavings.getId() + "/deposit")
                        .param("memberId", testMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("9999999"))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // 서비스에서 IllegalStateException
    }

    @Test
    @DisplayName("존재하지 않는 저축 목표 납입 → 4xx/5xx")
    void 존재하지_않는_저축목표_납입() throws Exception {
        mockMvc.perform(patch(SAVINGS_URL + "/999999/deposit")
                        .param("memberId", testMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10000"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
