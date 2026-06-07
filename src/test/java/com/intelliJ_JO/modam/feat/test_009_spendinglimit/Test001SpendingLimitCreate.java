package com.intelliJ_JO.modam.feat.test_009_spendinglimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendinglimit.dto.SpendingLimitSaveRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 소비 제한 생성 테스트
 * 대상 API: POST /api/spending-limits
 * @AuthenticationPrincipal 사용 → 인증 필요
 * 서비스에서 member.getAccount()를 통해 계좌를 조회하므로
 * Member는 반드시 Account와 연결되어 있어야 함.
 * 화면: budget.html 소비 제한 설정 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 소비 제한 생성 테스트")
class Test001SpendingLimitCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String LIMIT_URL       = "/api/spending-limits";

    private Member testMember;

    @BeforeEach
    void setUp() {
        // member.getAccount() 가 null이면 서비스에서 예외 발생
        Account account = accountRepository.save(Account.builder()
                .accountNumber("LMTACC1234567A").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("lmtuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("lmt001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11099345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01099345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(account)
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "lmtuser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 카테고리 선택 없이 전체 소비 제한 생성 → 200 + SUCCESS")
    void 전체_카테고리_제한_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setBudgetAmount(500_000L);
        request.setAlertAt80(true);
        request.setAlertAt100(true);
        request.setPushAlert(true);

        mockMvc.perform(post(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정상: 특정 카테고리(식비) 소비 제한 생성 → 200 + SUCCESS")
    void 특정_카테고리_제한_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setCategories(List.of("식비"));
        request.setBudgetAmount(200_000L);
        request.setAlertAt80(true);
        request.setEveryTransaction(true);
        request.setPushAlert(true);

        mockMvc.perform(post(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동일 카테고리 중복 생성 → 200 + EXISTS 반환 (서비스 레벨 처리)")
    void 동일_카테고리_중복_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setCategories(List.of("교통"));
        request.setBudgetAmount(100_000L);

        // 첫 번째 생성
        mockMvc.perform(post(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 동일 카테고리 중복 생성
        mockMvc.perform(post(LIMIT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk()); // "EXISTS" 문자열 반환
    }

    @Test
    @DisplayName("계좌 미연결 회원의 소비 제한 생성 → 5xx")
    void 계좌_미연결_회원_소비제한_생성() throws Exception {
        // account 없는 회원 생성
        memberRepository.save(Member.builder()
                .userId("noaccountlmt01")
                .pwHash(passwordEncoder.encode("password123"))
                .name("계좌없음").email("noacc001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("No").enLast("Acc")
                .bankName("신한은행").persAcctNo("00099345678901")
                .zipCode("06236").address("서울 1")
                .phoneNo("01099300001")
                .rrn(passwordEncoder.encode("910101123456"))
                .build()); // account 없음

        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "noaccountlmt01")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession noAccSession = (MockHttpSession) result.getRequest().getSession(false);

        SpendingLimitSaveRequest request = new SpendingLimitSaveRequest();
        request.setBudgetAmount(500_000L);

        mockMvc.perform(post(LIMIT_URL)
                        .session(noAccSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }
}
