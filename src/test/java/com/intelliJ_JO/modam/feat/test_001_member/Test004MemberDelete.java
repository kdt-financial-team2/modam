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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-004 | 회원 탈퇴 테스트
 * 대상 API: DELETE /api/member/{memberId}
 * 마이페이지 회원 탈퇴 화면(mypage/withdrawal.html) 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-004 | 회원 탈퇴 테스트")
class Test004MemberDelete {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL  = "/login";
    private static final String MEMBER_URL = "/api/member";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("testuser004")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("test004@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Gildong")
                .enLast("Hong")
                .bankName("신한은행")
                .persAcctNo("11012345678904")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .phoneNo("01012345674")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser004")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 로그인 후 회원 탈퇴 → 200")
    void 정상_회원_탈퇴() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(delete(MEMBER_URL + "/" + testMember.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("탈퇴 후 해당 회원 조회 → 200 (soft delete: active=false 이지만 findById는 여전히 반환)")
    void 탈퇴_후_회원_조회_여전히_가능() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // 탈퇴 실행 (deactivate → active=false, 실제 행 삭제 없음)
        mockMvc.perform(delete(MEMBER_URL + "/" + testMember.getId()).session(session))
                .andExpect(status().isOk());

        // findById 는 active 필터 없음 → 탈퇴된 회원도 200 반환
        mockMvc.perform(get(MEMBER_URL + "/" + testMember.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비로그인 상태로 회원 탈퇴 → 200 (MemberController 인증 미적용, permitAll)")
    void 비로그인_탈퇴_가능() throws Exception {
        // MemberController 는 @AuthenticationPrincipal 없음 → 인증 없이 삭제 가능
        mockMvc.perform(delete(MEMBER_URL + "/" + testMember.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 회원 탈퇴 시도 → 4xx 오류")
    void 존재하지_않는_회원_탈퇴() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(delete(MEMBER_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
