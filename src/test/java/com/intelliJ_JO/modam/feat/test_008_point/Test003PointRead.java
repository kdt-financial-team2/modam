package com.intelliJ_JO.modam.feat.test_008_point;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 포인트 내역 조회 및 현재 잔액 조회 테스트 (보조 기능)
 * 대상 API:
 *   GET /api/points          - 포인트 내역 목록
 *   GET /api/points/current  - 현재 보유 포인트
 * @AuthenticationPrincipal 사용 → 인증 필요
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 포인트 조회 테스트")
class Test003PointRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CoupleRepository coupleRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";

    private Member testMember;

    @BeforeEach
    void setUp() {
        Account account = accountRepository.save(Account.builder()
                .accountNumber("PTACC3234567C").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("ptuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("pt003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11088345678903")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01088345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(account)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(testMember).inviteStatus(InviteStatus.ACCEPT).build());

        Couple couple = coupleRepository.save(Couple.builder()
                .account(account).inviteCode("GHI789").build());

        // 포인트 내역 2건 저장 (잔액 300P)
        pointRepository.save(PointHistory.builder()
                .couple(couple).type(PointType.SAVE)
                .reason(PointReason.ATTENDANCE).amt(100).aftBal(100).descrip("출석").build());
        pointRepository.save(PointHistory.builder()
                .couple(couple).type(PointType.SAVE)
                .reason(PointReason.SPEND_RECORD).amt(200).aftBal(300).descrip("소비기록").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "ptuser003")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 포인트 내역 목록 조회 → 200 + 2건 반환")
    void 정상_포인트_내역_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get("/api/points").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("정상: 현재 보유 포인트 조회 → 200 + 300P 반환")
    void 정상_현재_포인트_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get("/api/points/current").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(300));
    }

    @Test
    @DisplayName("Couple 없는 회원의 현재 포인트 조회 → 0 반환")
    void couple_없는_회원_포인트_조회() throws Exception {
        // Couple 없는 새 회원 생성 (GROUP 계좌 AccountMember 없음)
        Account soloAccount = accountRepository.save(Account.builder()
                .accountNumber("SOLO_ACC_PT001").accountType(AccountType.GROUP).build());
        memberRepository.save(Member.builder()
                .userId("solouser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("솔로").email("solo003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Solo").enLast("User")
                .bankName("국민은행").persAcctNo("55588345678903")
                .zipCode("06001").address("서울 1")
                .phoneNo("01066600003")
                .rrn(passwordEncoder.encode("850101123456"))
                .account(soloAccount)
                .build());

        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "solouser003")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession soloSession = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/api/points/current").session(soloSession))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }
}
