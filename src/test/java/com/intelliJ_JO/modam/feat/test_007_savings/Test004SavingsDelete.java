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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-004 | 저축 목표 삭제 테스트
 * 대상 API: DELETE /api/savings/{savingsId}
 * 인증 불필요 (anyRequest().permitAll() 설정)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-004 | 저축 목표 삭제 테스트")
class Test004SavingsDelete {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SavingsRepository savingsRepository;

    private Savings testSavings;

    @BeforeEach
    void setUp() {
        Account testAccount = accountRepository.save(Account.builder()
                .accountNumber("SAVACC4234567D").accountType(AccountType.GROUP).build());

        testSavings = savingsRepository.save(Savings.builder()
                .account(testAccount).goalName("삭제될 목표").saveType("기타")
                .targetAmount(100_000L).targetDate(LocalDate.now().plusMonths(1)).build());
    }

    @Test
    @DisplayName("정상: 저축 목표 삭제 → 200")
    void 정상_저축목표_삭제() throws Exception {
        mockMvc.perform(delete("/api/savings/" + testSavings.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 저축 목표 삭제 → 4xx")
    void 존재하지_않는_저축목표_삭제() throws Exception {
        mockMvc.perform(delete("/api/savings/999999"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("삭제 후 재삭제 시도 → 4xx")
    void 삭제_후_재삭제_시도() throws Exception {
        // 첫 번째 삭제
        mockMvc.perform(delete("/api/savings/" + testSavings.getId()))
                .andExpect(status().isOk());

        // 두 번째 삭제 시도
        mockMvc.perform(delete("/api/savings/" + testSavings.getId()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
