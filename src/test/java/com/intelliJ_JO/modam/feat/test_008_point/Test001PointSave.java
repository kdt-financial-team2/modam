package com.intelliJ_JO.modam.feat.test_008_point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSaveRequest;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 포인트 적립 테스트
 * 대상 API: POST /api/points/save
 * 포인트 서비스는 Couple 엔티티를 통해 커플 단위로 포인트를 관리.
 * @AuthenticationPrincipal 사용 → 인증 필요
 * 화면: point-shop.html 에서 포인트 적립 요청 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 포인트 적립 테스트")
class Test001PointSave {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CoupleRepository coupleRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String POINT_SAVE_URL = "/api/points/save";

    private Member testMember;

    @BeforeEach
    void setUp() {
        // GROUP 계좌 생성
        Account account = accountRepository.save(Account.builder()
                .accountNumber("PTACC1234567A").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("ptuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("pt001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11088345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01088345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(account)
                .build());

        // AccountMember (포인트 서비스가 findByMemberId로 GROUP 계좌를 탐색)
        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(testMember).inviteStatus(InviteStatus.ACCEPT).build());

        // Couple 엔티티 (포인트 히스토리에 필수)
        coupleRepository.save(Couple.builder()
                .account(account).inviteCode("ABC123").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "ptuser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 출석 포인트 적립 → 200")
    void 정상_출석_포인트_적립() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSaveRequest request = PointSaveRequest.builder()
                .reason(PointReason.ATTENDANCE)
                .amt(10)
                .descrip("출석 체크")
                .build();

        mockMvc.perform(post(POINT_SAVE_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amt").value(10));
    }

    @Test
    @DisplayName("중복 출석 체크 → 5xx (오늘 이미 출석 포인트 지급)")
    void 중복_출석_포인트_적립() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSaveRequest request = PointSaveRequest.builder()
                .reason(PointReason.ATTENDANCE)
                .amt(10)
                .descrip("출석 체크")
                .build();

        // 첫 번째 출석
        mockMvc.perform(post(POINT_SAVE_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 중복 출석 시도 — IllegalStateException → GlobalExceptionHandler → 409
        mockMvc.perform(post(POINT_SAVE_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("적립 금액 0 이하 → 400 (@Min(1))")
    void 적립금액_0이하() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSaveRequest request = PointSaveRequest.builder()
                .reason(PointReason.SPEND_RECORD)
                .amt(0)
                .descrip("소비 기록")
                .build();

        mockMvc.perform(post(POINT_SAVE_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reason 누락 → 400 (@NotNull)")
    void reason_누락() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSaveRequest request = PointSaveRequest.builder()
                .amt(10)
                .descrip("설명")
                .build();

        mockMvc.perform(post(POINT_SAVE_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비로그인 시 @AuthenticationPrincipal null → NPE (null 체크 없음 → MockMvc ServletException 전파)")
    void 비로그인_포인트_적립() throws Exception {
        PointSaveRequest request = PointSaveRequest.builder()
                .reason(PointReason.ATTENDANCE)
                .amt(10)
                .descrip("출석")
                .build();

        // @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc가 ServletException으로 전파
        assertThrows(Exception.class, () ->
            mockMvc.perform(post(POINT_SAVE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andReturn()
        );
    }
}
