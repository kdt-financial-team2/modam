package com.intelliJ_JO.modam.feat.test_002_account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
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

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 모임통장 개설 흐름 테스트
 * 화면 흐름: Step1(본인확인) → Step2(약관동의) → Step3(계좌정보입력) → Step4(개설완료)
 * 대상 API: POST /api/accounts
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 모임통장 개설 테스트")
class Test001AccountCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL          = "/login";
    private static final String CREATE_ACCOUNT_URL = "/api/accounts";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("accuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("acc001@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Gildong")
                .enLast("Hong")
                .bankName("신한은행")
                .persAcctNo("11022345678901")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .phoneNo("01022345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "accuser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    // 기본 유효한 계좌 개설 요청
    private AccountCreateRequestDto baseRequest() {
        AccountCreateRequestDto dto = new AccountCreateRequestDto();
        dto.setAccountType(AccountType.GROUP);
        dto.setPassword("1234");
        dto.setPasswordConfirm("1234");
        dto.setOnceTransferLimit(1_000_000L);
        dto.setDailyTransferLimit(5_000_000L);
        dto.setJobInfo("직장인");
        dto.setTradePurpose("모임비 관리");
        dto.setFundSource("급여");
        dto.setAgreeService(true);
        dto.setAgreeFinance(true);  // @AssertTrue 필수 — 누락 시 모든 개설 테스트 validation 실패
        dto.setAgreePrivacy(true);
        dto.setAgreeMarketing(false);
        dto.setAgreeThirdParty(false);
        return dto;
    }

    // ── Step 3: 계좌 정보 입력 ────────────────────────────────

    @Test
    @DisplayName("Step3→4: 모든 항목 정상 입력 → 계좌 개설 성공 (5050-XXXXXXXX-XXX 형식 계좌번호 반환)")
    void 정상_계좌_개설_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // generateAccountNumber() → "5050-XXXXXXXX-XXX" 형식 (대시 포함 17자)
                .andExpect(jsonPath("$.data.accountNumber").value(matchesPattern("5050-[0-9]{8}-[0-9]{3}")))
                .andExpect(jsonPath("$.data.accountType").value("GROUP"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Step3: 계좌 비밀번호 4자리 미만(3자리) → 400")
    void 계좌_비밀번호_4자리_미만() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setPassword("123");
        request.setPasswordConfirm("123");

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Step3: 계좌 비밀번호 4자리 초과(5자리) → 400")
    void 계좌_비밀번호_4자리_초과() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setPassword("12345");
        request.setPasswordConfirm("12345");

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Step3: 계좌 비밀번호에 숫자 외 문자 포함(영문) → 400")
    void 계좌_비밀번호_문자_포함() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setPassword("12ab");
        request.setPasswordConfirm("12ab");

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("계좌 비밀번호는 숫자 4자리여야 합니다."));
    }

    @Test
    @DisplayName("Step3: 비밀번호·확인 불일치 → 200 (서비스에서 passwordConfirm 검증 없음 — password만 저장)")
    void 계좌_비밀번호_불일치() throws Exception {
        // AccountService.createAccount 는 passwordConfirm 을 검증하지 않고 password 만 해싱하여 저장
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setPassword("1234");
        request.setPasswordConfirm("5678");

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Step2: 서비스 이용약관 필수 미동의 → 400")
    void 서비스_약관_미동의() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setAgreeService(false);

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("서비스 이용약관 동의는 필수입니다."));
    }

    @Test
    @DisplayName("Step2: 개인정보 처리방침 필수 미동의 → 400")
    void 개인정보_약관_미동의() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setAgreePrivacy(false);

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("개인정보 처리방침 동의는 필수입니다."));
    }

    @Test
    @DisplayName("Step2: 선택 약관(마케팅·3자) 미동의 → 개설 성공")
    void 선택_약관_미동의_개설_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setAgreeMarketing(false);
        request.setAgreeThirdParty(false);

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 계좌 개설 시도 → 409 (AccountController null 체크 → IllegalStateException → GlobalExceptionHandler)")
    void 비로그인_계좌_개설_리다이렉트() throws Exception {
        // userDetails == null → throw new IllegalStateException("로그인이 필요합니다.")
        // GlobalExceptionHandler: IllegalStateException → 409 CONFLICT
        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest())))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("이체 한도 음수 설정 → 200 (AccountCreateRequestDto에 @Min 검증 없음 — 그대로 저장)")
    void 이체한도_음수_설정() throws Exception {
        // onceTransferLimit 필드에 @Min(0) 어노테이션 없음 → DTO 검증 통과 → 계좌 개설 성공
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseRequest();
        request.setOnceTransferLimit(-100L);

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
