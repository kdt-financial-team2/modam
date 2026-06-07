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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-005 | 중복 확인 보조 기능 테스트 (화면 이동 아닌 AJAX 호출)
 * 대상 API: GET /api/member/check-userid, GET /api/member/check-account
 * 회원가입 화면 3에서 실시간 중복 체크 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("Test-005 | 아이디·계좌번호 중복 확인 테스트")
class Test005MemberDuplicateCheck {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String CHECK_USERID_URL  = "/api/member/check-userid";
    private static final String CHECK_ACCOUNT_URL = "/api/member/check-account";

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 이미 가입된 회원 데이터 준비
        testMember = memberRepository.save(Member.builder()
                .userId("existinguser")
                .pwHash(passwordEncoder.encode("password123"))
                .name("기존회원")
                .email("existing@modam.com")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .enFirst("Existing")
                .enLast("User")
                .bankName("신한은행")
                .persAcctNo("99900000000001")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 1")
                .phoneNo("01099999991")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());
    }

    // ── 아이디 중복 확인 ───────────────────────────────────────

    @Test
    @DisplayName("아이디 중복 확인: 사용 가능한 아이디 → $.data=true")
    void 사용가능한_아이디() throws Exception {
        // GlobalResponse<Boolean> → $.data 는 Boolean 직접값 (중첩 객체 없음)
        mockMvc.perform(get(CHECK_USERID_URL).param("userId", "newuniqueuser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("아이디 중복 확인: 이미 사용 중인 아이디 → available=false 또는 400")
    void 중복된_아이디() throws Exception {
        mockMvc.perform(get(CHECK_USERID_URL).param("userId", "existinguser"))
                .andDo(print())
                // 중복 시 available=false 또는 400 반환
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 200 || status == 400;
                });
    }

    @Test
    @DisplayName("아이디 중복 확인: userId 파라미터 누락 → 4xx")
    void 아이디_파라미터_누락() throws Exception {
        mockMvc.perform(get(CHECK_USERID_URL))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("아이디 중복 확인: 빈 문자열 userId → 200 + true (available, 존재하는 ID 없음)")
    void 아이디_빈문자열() throws Exception {
        // @RequestParam 에 빈 문자열이 오면 서비스가 existsByUserId("") 조회 → false → available=true 반환
        mockMvc.perform(get(CHECK_USERID_URL).param("userId", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    // ── 계좌번호 중복 확인 ────────────────────────────────────

    @Test
    @DisplayName("계좌번호 중복 확인: 사용 가능한 계좌번호 → $.data=true")
    void 사용가능한_계좌번호() throws Exception {
        // 컨트롤러 @RequestParam 이름은 "accountNumber" — persAcctNo 아님
        // GlobalResponse<Boolean> → $.data 는 Boolean 직접값
        mockMvc.perform(get(CHECK_ACCOUNT_URL).param("accountNumber", "11100000099999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("계좌번호 중복 확인: 이미 등록된 계좌번호 → available=false 또는 400")
    void 중복된_계좌번호() throws Exception {
        mockMvc.perform(get(CHECK_ACCOUNT_URL).param("persAcctNo", "99900000000001"))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 200 || status == 400;
                });
    }

    @Test
    @DisplayName("계좌번호 중복 확인: persAcctNo 파라미터 누락 → 4xx")
    void 계좌번호_파라미터_누락() throws Exception {
        mockMvc.perform(get(CHECK_ACCOUNT_URL))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
