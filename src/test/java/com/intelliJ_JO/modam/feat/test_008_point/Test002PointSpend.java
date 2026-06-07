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
import com.intelliJ_JO.modam.domain.point.entity.PointHistory;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import com.intelliJ_JO.modam.domain.point.repository.PointRepository;
import com.intelliJ_JO.modam.domain.point.dto.request.PointSpendRequest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 포인트 사용 테스트
 * 대상 API: POST /api/points/spend
 * @AuthenticationPrincipal 사용 → 인증 필요
 * 포인트 잔액 부족 시 IllegalStateException 발생 (5xx)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 포인트 사용 테스트")
class Test002PointSpend {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CoupleRepository coupleRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String POINT_SPEND_URL = "/api/points/spend";

    private Couple testCouple;
    private Member testMember;

    @BeforeEach
    void setUp() {
        Account account = accountRepository.save(Account.builder()
                .accountNumber("PTACC2234567B").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("ptuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("pt002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11088345678902")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01088345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(account)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(testMember).inviteStatus(InviteStatus.ACCEPT).build());

        testCouple = coupleRepository.save(Couple.builder()
                .account(account).inviteCode("DEF456").build());

        // 포인트 잔액 500P 세팅 (적립 내역 저장)
        pointRepository.save(PointHistory.builder()
                .couple(testCouple)
                .type(PointType.SAVE)
                .reason(PointReason.ATTENDANCE)
                .amt(500)
                .aftBal(500)
                .descrip("초기 잔액 세팅")
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "ptuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 보유 포인트 내 사용 → 200")
    void 정상_포인트_사용() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSpendRequest request = PointSpendRequest.builder()
                .reason(PointReason.ITEM_PURCHASE)
                .amt(100)
                .descrip("아이템 구매")
                .build();

        mockMvc.perform(post(POINT_SPEND_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aftBal").value(400));
    }

    @Test
    @DisplayName("포인트 잔액 초과 사용 → 5xx (IllegalStateException)")
    void 포인트_잔액_초과_사용() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSpendRequest request = PointSpendRequest.builder()
                .reason(PointReason.THEME_PURCHASE)
                .amt(9999)
                .descrip("잔액 초과 사용 시도")
                .build();

        mockMvc.perform(post(POINT_SPEND_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict()); // IllegalStateException → GlobalExceptionHandler → 409
    }

    @Test
    @DisplayName("사용 금액 0 → 400 (@Min(1))")
    void 사용금액_0() throws Exception {
        MockHttpSession session = loginAndGetSession();

        PointSpendRequest request = PointSpendRequest.builder()
                .reason(PointReason.ITEM_PURCHASE)
                .amt(0)
                .descrip("0원 사용 시도")
                .build();

        mockMvc.perform(post(POINT_SPEND_URL)
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

        PointSpendRequest request = PointSpendRequest.builder()
                .amt(50)
                .descrip("사유 없음")
                .build();

        mockMvc.perform(post(POINT_SPEND_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
