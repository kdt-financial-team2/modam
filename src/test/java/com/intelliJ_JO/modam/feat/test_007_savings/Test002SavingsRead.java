package com.intelliJ_JO.modam.feat.test_007_savings;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.savings.entity.Savings;
import com.intelliJ_JO.modam.domain.savings.repository.SavingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 계좌별 저축 목표 목록 조회 테스트 (보조 기능)
 * 대상 API: GET /api/savings/account/{accountId}
 * savings.html 화면 로드 시 목표 목록 요청 흐름
 * 인증 불필요 (anyRequest().permitAll() 설정)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 저축 목표 목록 조회 테스트")
class Test002SavingsRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SavingsRepository savingsRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SAVACC2234567B").accountType(AccountType.GROUP).build());

        // 저축 목표 2건 미리 저장
        savingsRepository.save(Savings.builder()
                .account(testAccount).goalName("제주도 여행").saveType("여행")
                .targetAmount(500_000L).targetDate(LocalDate.now().plusMonths(3)).build());
        savingsRepository.save(Savings.builder()
                .account(testAccount).goalName("결혼 자금").saveType("결혼")
                .targetAmount(10_000_000L).targetDate(LocalDate.now().plusYears(1)).build());
    }

    @Test
    @DisplayName("정상: 계좌별 저축 목표 목록 조회 → 200 + 2건 반환")
    void 정상_저축_목록_조회() throws Exception {
        mockMvc.perform(get("/api/savings/account/" + testAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("저축 목표 없는 계좌 조회 → 빈 배열 반환")
    void 저축목표_없는_계좌_조회() throws Exception {
        Account emptyAccount = accountRepository.save(Account.builder()
                .accountNumber("EMPTY_SAVACC001").accountType(AccountType.GROUP).build());

        mockMvc.perform(get("/api/savings/account/" + emptyAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("조회된 저축 목표에 goalName 포함 여부 확인")
    void 저축_목표_데이터_필드_확인() throws Exception {
        mockMvc.perform(get("/api/savings/account/" + testAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].goalName").exists());
    }
}
