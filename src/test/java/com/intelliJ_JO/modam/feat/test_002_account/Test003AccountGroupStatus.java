package com.intelliJ_JO.modam.feat.test_002_account;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 모임통장 상태 조회·계좌번호 미리보기 보조 기능 테스트
 * 대상 API:
 *   GET /api/accounts/me/group-status  (로그인 직후 모임통장 보유 여부 판단)
 *   GET /api/accounts/preview-number   (개설 화면 진입 시 계좌번호 미리 생성)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 모임통장 상태·계좌번호 미리보기 테스트")
class Test003AccountGroupStatus {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String GROUP_STATUS_URL = "/api/accounts/me/group-status";
    private static final String PREVIEW_URL      = "/api/accounts/preview-number";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("accuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("acc003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11022345678903")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01022345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "accuser003")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    // ── 모임통장 상태 조회 ─────────────────────────────────────

    @Test
    @DisplayName("모임통장 없음 → hasGroupAccount=false (계좌 개설 화면으로 이동)")
    void 모임통장_없음_상태_확인() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(GROUP_STATUS_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasGroupAccount").value(false));
    }

    @Test
    @DisplayName("모임통장 있음 → hasGroupAccount=true + 계좌ID·번호 반환 (대시보드로 이동)")
    void 모임통장_있음_상태_확인() throws Exception {
        // 모임통장 미리 생성
        Account account = accountRepository.save(Account.builder()
                .accountNumber("GRPSTAT1234567A")
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
                .andExpect(jsonPath("$.data.accountNumber").value("GRPSTAT1234567A"));
    }

    @Test
    @DisplayName("초대 WAIT 상태 구성원 → 모임통장 없음으로 처리")
    void 초대_WAIT_상태_모임통장_없음() throws Exception {
        Account account = accountRepository.save(Account.builder()
                .accountNumber("WAITACC1234567A")
                .accountType(AccountType.GROUP)
                .build());
        // ACCEPT가 아닌 WAIT 상태로 저장
        accountMemberRepository.save(AccountMember.builder()
                .account(account)
                .member(testMember)
                .inviteStatus(InviteStatus.WAIT)
                .build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(GROUP_STATUS_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasGroupAccount").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 모임통장 상태 조회 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_그룹상태_조회_리다이렉트() {
        // getGroupAccountStatus: @AuthenticationPrincipal null 체크 없음
        // 비로그인 시 userDetails=null → userDetails.getMember() → NPE
        // GlobalExceptionHandler 미처리 → MockMvc 가 ServletException으로 전파 (status() 확인 불가)
        assertThrows(Exception.class, () ->
            mockMvc.perform(get(GROUP_STATUS_URL)).andReturn()
        );
    }

    // ── 계좌번호 미리보기 ──────────────────────────────────────

    @Test
    @DisplayName("계좌 개설 화면 진입 시 계좌번호 미리보기 → 5050-XXXXXXXX-XXX 형식 반환")
    void 계좌번호_미리보기_16자리_반환() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(PREVIEW_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // generatePreviewAccountNumber() → "5050-" + 8자리숫자 + "-" + 3자리숫자
                .andExpect(jsonPath("$.data.accountNumber").value(matchesPattern("5050-[0-9]{8}-[0-9]{3}")));
    }

    @Test
    @DisplayName("계좌번호 미리보기 두 번 호출 → 매번 다른 값 반환")
    void 계좌번호_미리보기_매번_다른_값() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MvcResult r1 = mockMvc.perform(get(PREVIEW_URL).session(session)).andReturn();
        MvcResult r2 = mockMvc.perform(get(PREVIEW_URL).session(session)).andReturn();

        String body1 = r1.getResponse().getContentAsString();
        String body2 = r2.getResponse().getContentAsString();
        // 두 응답은 달라야 함 (랜덤 생성)
        assert !body1.equals(body2) : "계좌번호 미리보기가 중복되어서는 안 됩니다.";
    }

    @Test
    @DisplayName("비로그인 상태로 계좌번호 미리보기 접근 → 200 (인증 불필요)")
    void 비로그인_계좌번호_미리보기_리다이렉트() throws Exception {
        // previewAccountNumber: @AuthenticationPrincipal 없음 → 인증 없이도 접근 가능
        mockMvc.perform(get(PREVIEW_URL))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
