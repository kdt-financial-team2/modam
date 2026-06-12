package com.intelliJ_JO.modam.feat.test_001_member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.member.dto.MemberUpdateRequest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 회원 정보 수정 테스트
 * 대상 API: PATCH /api/member/{memberId}
 * 마이페이지 프로필 편집 화면(mypage/profile.html) 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 회원 정보 수정 테스트")
class Test003MemberUpdate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL  = "/login";
    private static final String MEMBER_URL = "/api/member";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("testuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동")
                .email("test003@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Gildong")
                .enLast("Hong")
                .bankName("신한은행")
                .persAcctNo("11012345678903")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .phoneNo("01012345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "testuser003")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 이메일 수정 → 200")
    void 정상_이메일_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setEmail("updated003@modam.com");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 주소 수정 → 200")
    void 정상_주소_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setZipCode("05505");
        request.setAddress("서울시 송파구 올림픽로 300");
        request.setAddressDetail("202호");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 비밀번호 변경 → 200")
    void 정상_비밀번호_변경() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setPassword("newpassword123");
        request.setPasswordConfirm("newpassword123");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이메일 형식 오류(@없음) 수정 시도 → 400")
    void 이메일_형식_오류_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setEmail("invalidemail");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("휴대폰 번호 형식 오류(하이픈 포함) 수정 시도 → 400")
    void 휴대폰번호_형식_오류_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setPhoneNo("010-1234-5678");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("계좌번호 하이픈 포함 수정 시도 → 400")
    void 계좌번호_형식_오류_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setPersAcctNo("1101-234-5678");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("우편번호 5자리 미만 수정 시도 → 400")
    void 우편번호_형식_오류_수정() throws Exception {
        MockHttpSession session = loginAndGetSession();

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setZipCode("062");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 회원 정보 수정 시도 → 200 (MemberController 인증 미적용, permitAll)")
    void 비로그인_수정_가능() throws Exception {
        // MemberController 는 @AuthenticationPrincipal 없음 → anyRequest().permitAll() 적용
        // 인증 없이도 수정 가능 (200 반환)
        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setEmail("nologin@modam.com");

        mockMvc.perform(patch(MEMBER_URL + "/" + testMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
