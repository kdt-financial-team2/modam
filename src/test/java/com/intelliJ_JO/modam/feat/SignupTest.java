package com.intelliJ_JO.modam.feat;

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

@Disabled
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("회원가입 흐름 테스트 (화면 1→2→3→4→5)")
class SignupTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SIGNUP_URL = "/api/member";

    // 기본 유효한 요청 빌더 — 각 테스트에서 필요한 필드만 오버라이드
    private MemberCreateRequest.MemberCreateRequestBuilder baseRequest() {
        return MemberCreateRequest.builder()
                .userId("testuser1")
                .pw("password123")
                .pwConfirm("password123")
                .name("홍길동")
                .enFirst("Gildong")
                .enLast("Hong")
                .email("test1@modam.com")
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

    // ───────────────────────────────────────────────
    // 화면 2 — 약관 동의
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("화면 2→3→4: 모든 조건 충족 시 회원가입 성공 → 200")
    void 정상_회원가입_성공() throws Exception {
        MemberCreateRequest request = baseRequest().build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("testuser1"));
    }

    @Test
    @DisplayName("화면 2: 만 14세 이상 필수 약관 미동의 → 400")
    void 필수_약관_만14세_미동의() throws Exception {
        MemberCreateRequest request = baseRequest()
                .agreeAge(false)
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 모담 이용약관 필수 동의 미동의 → 400")
    void 필수_약관_서비스_미동의() throws Exception {
        MemberCreateRequest request = baseRequest()
                .agreeService(false)
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 개인정보 수집 및 이용 필수 동의 미동의 → 400")
    void 필수_약관_개인정보_미동의() throws Exception {
        MemberCreateRequest request = baseRequest()
                .agreePrivacy(false)
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 전자금융거래 이용약관 필수 동의 미동의 → 400")
    void 필수_약관_전자금융_미동의() throws Exception {
        MemberCreateRequest request = baseRequest()
                .agreeFinance(false)
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 2: 선택 약관(마케팅·3자 제공) 미동의 → 가입 성공")
    void 선택_약관_미동의_가입_성공() throws Exception {
        MemberCreateRequest request = baseRequest()
                .notif(false)
                .agreeThirdParty(false)
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ───────────────────────────────────────────────
    // 화면 3 — 정보 입력
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("화면 3: 비밀번호와 비밀번호 확인 불일치 → 400")
    void 비밀번호_확인_불일치() throws Exception {
        MemberCreateRequest request = baseRequest()
                .pw("password123")
                .pwConfirm("differentPW!")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("화면 3: 아이디 중복 시 → 400")
    void 아이디_중복_가입() throws Exception {
        // 첫 번째 가입
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        // 동일 아이디로 두 번째 가입 시도
        MemberCreateRequest duplicate = baseRequest()
                .email("other@modam.com")
                .phoneNo("01098765432")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicate)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("화면 3: 이메일 중복 시 → 400")
    void 이메일_중복_가입() throws Exception {
        // 첫 번째 가입
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        // 동일 이메일로 두 번째 가입 시도
        MemberCreateRequest duplicate = baseRequest()
                .userId("otheruser1")
                .phoneNo("01098765432")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicate)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("화면 3: 휴대폰 번호 중복 시 → 400")
    void 휴대폰번호_중복_가입() throws Exception {
        // 첫 번째 가입
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(baseRequest().build())))
                .andExpect(status().isOk());

        // 동일 휴대폰 번호로 두 번째 가입 시도
        MemberCreateRequest duplicate = baseRequest()
                .userId("otheruser2")
                .email("other2@modam.com")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicate)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 휴대폰 번호입니다."));
    }

    @Test
    @DisplayName("화면 3: 이름 누락(필수 필드 공백) → 400")
    void 필수_필드_이름_누락() throws Exception {
        MemberCreateRequest request = baseRequest()
                .name("")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 3: 이메일 형식 오류 → 400")
    void 이메일_형식_오류() throws Exception {
        MemberCreateRequest request = baseRequest()
                .email("not-an-email")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("화면 3: 휴대폰 번호 형식 오류 → 400")
    void 휴대폰번호_형식_오류() throws Exception {
        MemberCreateRequest request = baseRequest()
                .phoneNo("010-1234-5678") // 하이픈 포함 → 실패해야 함
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ───────────────────────────────────────────────
    // 화면 4 — 계좌 연결
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("화면 4: 계좌번호에 문자 포함 시 → 400")
    void 계좌번호_문자_포함() throws Exception {
        MemberCreateRequest request = baseRequest()
                .persAcctNo("1101-234-5678") // 하이픈 포함 → 실패해야 함
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("계좌번호는 숫자만 입력 가능합니다."));
    }

    @Test
    @DisplayName("화면 4: 우편번호 5자리 숫자 아닌 경우 → 400")
    void 우편번호_형식_오류() throws Exception {
        MemberCreateRequest request = baseRequest()
                .zipCode("062") // 5자리 미만 → 실패해야 함
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
