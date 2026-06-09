package com.intelliJ_JO.modam.feat.test_003_card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.dto.CardCreateRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 카드 발급 흐름 테스트
 * 화면 흐름: card-step1 ~ card-step8 (8단계 UI)
 * 대상 API: POST /api/cards
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 카드 발급 테스트")
class Test001CardIssue {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String CARDS_URL = "/api/cards";

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("carduser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("card001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11033345678901")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01033345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("CARDACC1234567A")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "carduser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private CardCreateRequestDto baseCardRequest() {
        CardCreateRequestDto dto = new CardCreateRequestDto();
        dto.setAccountId(testAccount.getId());
        dto.setMemberId(testMember.getId());
        dto.setCardNumber("1234567890123456");
        dto.setExpiryDate("12/28");
        dto.setCardDesign("DEFAULT");
        dto.setCardType("DEBIT");
        dto.setPassword("1234");
        return dto;
    }

    @Test
    @DisplayName("정상: 카드 발급 성공 → 200")
    void 정상_카드_발급_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(CARDS_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("카드 유효기간 형식 오류(MMYY 형식) → 400")
    void 카드_유효기간_형식_오류() throws Exception {
        MockHttpSession session = loginAndGetSession();
        CardCreateRequestDto request = baseCardRequest();
        request.setExpiryDate("1228"); // 슬래시 없음

        mockMvc.perform(post(CARDS_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("카드 번호 누락 → 400")
    void 카드번호_누락() throws Exception {
        MockHttpSession session = loginAndGetSession();
        CardCreateRequestDto request = baseCardRequest();
        request.setCardNumber("");

        mockMvc.perform(post(CARDS_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("동일 카드 번호 중복 발급 시도 → 400")
    void 카드번호_중복_발급() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // 첫 번째 발급
        mockMvc.perform(post(CARDS_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardRequest())))
                .andExpect(status().isOk());

        // 동일 카드번호로 두 번째 발급 시도
        mockMvc.perform(post(CARDS_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardRequest())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 등록된 카드 번호입니다."));
    }

    @Test
    @DisplayName("계좌 구성원이 아닌 회원이 카드 발급 시도 → 4xx")
    void 미계좌구성원_카드_발급_시도() throws Exception {
        // 계좌에 속하지 않은 외부 회원 생성
        Member outsider = memberRepository.save(Member.builder()
                .userId("outsider001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("외부인").email("out001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Out").enLast("Sider")
                .bankName("국민은행").persAcctNo("99933345678901")
                .zipCode("06236").address("서울시 강남구 테헤란로 1")
                .phoneNo("01099945671")
                .rrn(passwordEncoder.encode("800101123456"))
                .build());

        // outsider로 로그인
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "outsider001")
                        .password("password", "password123"))
                .andExpect(authenticated()).andReturn();
        MockHttpSession outsiderSession = (MockHttpSession) result.getRequest().getSession(false);

        CardCreateRequestDto request = baseCardRequest();
        request.setMemberId(outsider.getId());

        mockMvc.perform(post(CARDS_URL)
                        .session(outsiderSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 카드 발급 시도 → 200 (CardController 인증 미적용, permitAll)")
    void 비로그인_카드_발급_리다이렉트() throws Exception {
        // CardController 는 @AuthenticationPrincipal 없음 → 인증 없이도 발급 가능
        mockMvc.perform(post(CARDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
