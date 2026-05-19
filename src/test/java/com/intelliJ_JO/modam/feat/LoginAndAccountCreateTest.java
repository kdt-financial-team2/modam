package com.intelliJ_JO.modam.feat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("로그인 및 모임통장 개설 흐름 테스트 (화면 1→2→3→4→5)")
class LoginAndAccountCreateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL          = "/login";
    private static final String GROUP_STATUS_URL   = "/api/accounts/me/group-status";
    private static final String PREVIEW_NUMBER_URL = "/api/accounts/preview-number";
    private static final String CREATE_ACCOUNT_URL = "/api/accounts";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("testuser")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("test@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Gildong")
                .enLast("Hong")
                .bankName("신한은행")
                .persAcctNo("11012345678901")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .phoneNo("01012345678")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    // 로그인 후 세션 반환 헬퍼
    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    // 유효한 계좌 개설 요청 헬퍼
    private AccountCreateRequestDto baseAccountRequest() {
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
        dto.setAgreePrivacy(true);
        dto.setAgreeMarketing(false);
        dto.setAgreeThirdParty(false);
        return dto;
    }

    // ─────────────────────────────────────────────────────
    // 1번 화면 → 2번 화면: 로그인
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("2번 화면: 아이디·비밀번호 정상 입력 → 로그인 성공")
    void 정상_로그인_성공() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser")
                        .password("password", "password123"))
                .andDo(print())
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("2번 화면: 존재하지 않는 아이디 입력 → 로그인 실패")
    void 존재하지_않는_아이디_로그인_실패() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "wronguser")
                        .password("password", "password123"))
                .andDo(print())
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("2번 화면: 비밀번호 오류 입력 → 로그인 실패")
    void 비밀번호_오류_로그인_실패() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser")
                        .password("password", "wrongpassword"))
                .andDo(print())
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("2번 화면: 로그인 후 모임통장 없음 → hasGroupAccount=false (3번 화면으로 이동)")
    void 로그인_후_모임통장_없으면_계좌개설_화면으로() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(GROUP_STATUS_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasGroupAccount").value(false))
                .andExpect(jsonPath("$.data.accountId").doesNotExist())
                .andExpect(jsonPath("$.data.accountNumber").doesNotExist());
    }

    @Test
    @DisplayName("2번 화면: 로그인 후 모임통장 있음 → hasGroupAccount=true (메인 대시보드로 이동)")
    void 로그인_후_모임통장_있으면_대시보드로() throws Exception {
        // 모임통장 미리 생성
        Account account = accountRepository.save(Account.builder()
                .accountNumber("TESTACC123456AB")
                .accountType(AccountType.GROUP)
                .build());
        accountMemberRepository.save(AccountMember.builder()
                .account(account)
                .member(testMember)
                .inviteStatus(InviteStatus.ACCEPT)
                .build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(GROUP_STATUS_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasGroupAccount").value(true))
                .andExpect(jsonPath("$.data.accountId").isNumber())
                .andExpect(jsonPath("$.data.accountNumber").value("TESTACC123456AB"));
    }

    // ─────────────────────────────────────────────────────
    // 3번 화면 → 4번 화면 진입: 계좌번호 미리 보기
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("3번 화면→4번 화면: 계좌개설 버튼 클릭 시 계좌번호 16자리 자동생성 반환")
    void 계좌번호_미리보기_반환() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(PREVIEW_NUMBER_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(matchesPattern("[A-Z0-9]{16}")));
    }

    @Test
    @DisplayName("3번 화면→4번 화면: 비로그인 상태로 preview-number 접근 → 403")
    void 비로그인_계좌번호_미리보기_접근_불가() throws Exception {
        mockMvc.perform(get(PREVIEW_NUMBER_URL))
                .andDo(print())
                .andExpect(status().is3xxRedirection()); // Security → /login 리다이렉트
    }

    // ─────────────────────────────────────────────────────
    // 4번 화면: 계좌 개설 상세 입력 → 계좌 개설 버튼
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("4번 화면: 모든 항목 정상 입력 → 계좌 개설 성공 (5번 화면으로 이동)")
    void 정상_계좌_개설_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseAccountRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(matchesPattern("[A-Z0-9]{16}")))
                .andExpect(jsonPath("$.data.accountType").value("GROUP"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.onceTransferLimit").value(1_000_000))
                .andExpect(jsonPath("$.data.dailyTransferLimit").value(5_000_000));
    }

    @Test
    @DisplayName("4번 화면: 계좌 비밀번호 4자리 미만 → 400")
    void 계좌_비밀번호_자리수_부족() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 계좌 비밀번호 4자리 초과 → 400")
    void 계좌_비밀번호_자리수_초과() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 계좌 비밀번호에 숫자 외 문자 포함 → 400")
    void 계좌_비밀번호_문자_포함() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 계좌 비밀번호와 비밀번호 확인 불일치 → 400")
    void 계좌_비밀번호_불일치() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
        request.setPassword("1234");
        request.setPasswordConfirm("5678");

        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("계좌 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("4번 화면: 서비스 이용약관 필수 미동의 → 400")
    void 서비스_이용약관_미동의() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 개인정보 처리방침 필수 미동의 → 400")
    void 개인정보_처리방침_미동의() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 선택 약관(마케팅·3자) 미동의 → 계좌 개설 성공")
    void 선택_약관_미동의_계좌_개설_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();
        AccountCreateRequestDto request = baseAccountRequest();
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
    @DisplayName("4번 화면: 비로그인 상태로 계좌 개설 시도 → 리다이렉트")
    void 비로그인_계좌_개설_시도() throws Exception {
        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseAccountRequest())))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    // ─────────────────────────────────────────────────────
    // 5번 화면: 완료 → 메인 대시보드 이동 확인
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("5번 화면: 계좌 개설 완료 후 group-status 재조회 → hasGroupAccount=true (메인 대시보드로 이동)")
    void 계좌개설_완료_후_대시보드_이동() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // 4번 화면 — 계좌 개설
        mockMvc.perform(post(CREATE_ACCOUNT_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseAccountRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5번 화면 — 완료 후 대시보드 이동 버튼: group-status 재확인
        mockMvc.perform(get(GROUP_STATUS_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasGroupAccount").value(true))
                .andExpect(jsonPath("$.data.accountId").isNumber())
                .andExpect(jsonPath("$.data.accountNumber").value(matchesPattern("[A-Z0-9]{16}")));
    }
}
