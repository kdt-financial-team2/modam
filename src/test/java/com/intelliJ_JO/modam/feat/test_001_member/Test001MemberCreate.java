package com.intelliJ_JO.modam.feat.test_001_member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.member.dto.MemberCreateRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 회원가입 흐름 테스트
 * 화면 흐름: 랜딩(1) → 약관동의(2) → 정보입력(3) → 계좌연결(4) → 완료(5)
 * 대상 API: POST /api/member
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("Test-001 | 회원가입 테스트")
class Test001MemberCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String SIGNUP_URL = "/api/member";

    // 기본 유효한 회원가입 요청 빌더
    private MemberCreateRequest.MemberCreateRequestBuilder baseRequest() {
        return MemberCreateRequest.builder()
                .userId("testuser001")
                .pw("password123")
                .pwConfirm("password123")
                .name("홍길동")
                .enFirst("Gildong")
                .enLast("Hong")
                .email("test001@modam.com")
                .phoneNo("01012345671")
                .zipCode("06236")
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101호")
                .bankName("신한은행")
                .persAcctNo("11012345678901")
                .rrn("900101123456")
                .agreeAge(true)
                .agreeService(true)
                .agreePrivacy(true)
                .agreeFinance(true)
                .notif(false)
                .agreeThirdParty(false);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ── 화면 2: 약관 동의 ──────────────────────────────────────

    @Test
    @DisplayName("화면 2→3→4: 모든 조건 충족 → 회원가입 성공 (200)")
    void 정상_회원가입_성공() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("testuser001"));
    }

    @Test
    @DisplayName("화면 2: 만 14세 이상 필수 약관 미동의 → 400")
    void 만14세_약관_미동의() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().agreeAge(false).build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 모담 이용약관 필수 미동의 → 400")
    void 서비스_약관_미동의() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().agreeService(false).build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 개인정보 수집·이용 필수 미동의 → 400")
    void 개인정보_약관_미동의() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().agreePrivacy(false).build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 전자금융거래 이용약관 필수 미동의 → 400")
    void 전자금융_약관_미동의() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().agreeFinance(false).build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 선택 약관(마케팅·3자 제공) 미동의 → 회원가입 성공 (200)")
    void 선택약관_미동의_가입_성공() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().notif(false).agreeThirdParty(false).build())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 화면 3: 정보 입력 ──────────────────────────────────────

    @Test
    @DisplayName("화면 3: 비밀번호·확인 불일치 → 400")
    void 비밀번호_확인_불일치() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().pw("password123").pwConfirm("different!").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("화면 3: 아이디 중복 → 400")
    void 아이디_중복_가입() throws Exception {
        // 첫 번째 가입
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        // 동일 아이디로 두 번째 가입 시도
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest()
                                .email("other001@modam.com")
                                .phoneNo("01098765431")
                                .build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("화면 3: 이메일 중복 → 400")
    void 이메일_중복_가입() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest()
                                .userId("otheruser001")
                                .phoneNo("01098765431")
                                .build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("화면 3: 휴대폰 번호 중복 → 400")
    void 휴대폰번호_중복_가입() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest()
                                .userId("otheruser002")
                                .email("other002@modam.com")
                                .build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 휴대폰 번호입니다."));
    }

    @Test
    @DisplayName("화면 3: 이름 누락(필수 필드 공백) → 400")
    void 이름_누락() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().name("").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 3: 이메일 형식 오류(@없음) → 400")
    void 이메일_형식_오류() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().email("invalidemail").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 3: 휴대폰 번호 형식 오류(하이픈 포함) → 400")
    void 휴대폰번호_형식_오류() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().phoneNo("010-1234-5678").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 화면 4: 계좌 연결 ──────────────────────────────────────

    @Test
    @DisplayName("화면 4: 계좌번호에 하이픈 포함 → 400")
    void 계좌번호_문자_포함() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().persAcctNo("1101-234-5678").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("계좌번호는 숫자만 입력 가능합니다."));
    }

    @Test
    @DisplayName("화면 4: 우편번호 5자리 미만 → 400")
    void 우편번호_형식_오류() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().zipCode("062").build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
