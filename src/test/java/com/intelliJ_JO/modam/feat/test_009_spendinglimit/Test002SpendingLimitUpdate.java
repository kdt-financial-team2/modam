package com.intelliJ_JO.modam.feat.test_009_spendinglimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
import com.intelliJ_JO.modam.domain.spendinglimit.entity.SpendingLimit;
import com.intelliJ_JO.modam.domain.spendinglimit.repository.SpendingLimitRepository;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 소비 제한 수정 테스트
 * 대상 API: PUT /api/spending-limits/update
 * @AuthenticationPrincipal 사용 → 인증 필요
 * 수정 대상이 없으면 IllegalArgumentException (5xx)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 소비 제한 수정 테스트")
class Test002SpendingLimitUpdate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SpendingLimitRepository spendingLimitRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String LIMIT_URL = "/api/spending-limits/update";

    private Account testAccount;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("LMTACC2234567B").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("lmtuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("lmt002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11099345678902")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01099345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(testAccount)
                .build());

        // 기존 소비 제한 데이터 저장 (식비, 200_000원)
        SpendingLimit existing = SpendingLimit.builder()
                .member(testMember)
                .account(testAccount)
                .category("식비")
                .build();
        existing.updateBudget(200_000L);
        spendingLimitRepository.save(existing);
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "lmtuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 기존 소비 제한 금액 수정 → 200 + UPDATED")
    void 정상_소비제한_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setCategories(List.of("식비"));
        request.setBudgetAmount(300_000L); // 200_000 → 300_000으로 변경
        request.setAlertAt80(true);
        request.setPushAlert(true);

        mockMvc.perform(put(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 설정 변경 → 200 + UPDATED")
    void 알림설정_변경() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setCategories(List.of("식비"));
        request.setBudgetAmount(200_000L);
        request.setAlertAt100(true); // 100% 알림만 켜기
        request.setEmailAlert(true);

        mockMvc.perform(put(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 → 5xx (수정할 소비 제한 없음)")
    void 존재하지_않는_카테고리_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setCategories(List.of("쇼핑")); // 저장된 식비와 다름
        request.setBudgetAmount(150_000L);

        mockMvc.perform(put(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // IllegalArgumentException → GlobalExceptionHandler → 400
    }
}
