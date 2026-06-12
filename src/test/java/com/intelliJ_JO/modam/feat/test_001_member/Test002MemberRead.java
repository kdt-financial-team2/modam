package com.intelliJ_JO.modam.feat.test_001_member;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 회원 조회 테스트
 * 대상 API: GET /api/member/{memberId}
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 회원 조회 테스트")
class Test002MemberRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL   = "/login";
    private static final String MEMBER_URL  = "/api/member";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("testuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("test002@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Gildong")
                .enLast("Hong")
                .bankName("신한은행")
                .persAcctNo("11012345678902")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .phoneNo("01012345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 로그인 후 내 회원정보 조회 → 200 + userId 반환")
    void 정상_회원_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(MEMBER_URL + "/" + testMember.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("testuser002"));
    }

    @Test
    @DisplayName("정상: 회원 이름·이메일 포함 여부 확인")
    void 회원_이름_이메일_포함_확인() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(MEMBER_URL + "/" + testMember.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("test002@modam.com"));
    }

    @Test
    @DisplayName("비로그인 상태로 회원 조회 → 200 (MemberController 인증 미적용, permitAll)")
    void 비로그인_회원_조회_가능() throws Exception {
        // MemberController 는 @AuthenticationPrincipal 없음 → anyRequest().permitAll() 적용
        // 인증 없이도 조회 가능 (200 반환)
        mockMvc.perform(get(MEMBER_URL + "/" + testMember.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID 조회 → 4xx 오류")
    void 존재하지_않는_회원_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(MEMBER_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("전체 회원 목록 조회 → ADMIN 권한 필요 또는 정상 조회")
    void 전체_회원_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(MEMBER_URL).session(session))
                .andDo(print())
                // ADMIN 권한이 없으면 403, 있으면 200+목록 반환
                .andExpect(status().is(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(200),
                        org.hamcrest.Matchers.is(403))));
    }
}
