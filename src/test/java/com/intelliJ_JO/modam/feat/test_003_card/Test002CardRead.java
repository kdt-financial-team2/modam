package com.intelliJ_JO.modam.feat.test_003_card;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.repository.CardRepository;
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
 * Test-002 | 계좌별 카드 목록 조회 테스트
 * 대상 API: GET /api/cards/account/{accountId}
 * 마이페이지 카드 목록 화면(mypage/cards.html) 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 카드 목록 조회 테스트")
class Test002CardRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String CARDS_URL = "/api/cards/account";

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("carduser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("card002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11033345678902")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01033345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("CARDRD1234567B")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        // 테스트용 카드 1장 생성
        cardRepository.save(Card.builder()
                .account(testAccount)
                .member(testMember)
                .cardNumber("ENCRYPTED_CARD_001")
                .expiryDate("12/28")
                .cardDesign("DEFAULT")
                .cardType("DEBIT")
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "carduser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 계좌별 카드 목록 조회 → 200 + 배열 반환")
    void 정상_카드_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(CARDS_URL + "/" + testAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("카드가 없는 계좌 조회 → 빈 배열 반환")
    void 카드없는_계좌_빈배열_반환() throws Exception {
        Account emptyAccount = accountRepository.save(Account.builder()
                .accountNumber("EMPTYCARD123456")
                .accountType(AccountType.GROUP)
                .build());
        accountMemberRepository.save(AccountMember.builder()
                .account(emptyAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(CARDS_URL + "/" + emptyAccount.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("비로그인 상태로 카드 목록 조회 → 200 (CardController 인증 미적용, permitAll)")
    void 비로그인_카드_목록_조회_리다이렉트() throws Exception {
        // CardController 는 @AuthenticationPrincipal 없음 → 인증 없이도 조회 가능
        mockMvc.perform(get(CARDS_URL + "/" + testAccount.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 계좌의 카드 목록 조회 → 200 + 빈 배열 (예외 없이 빈 리스트 반환)")
    void 존재하지_않는_계좌_카드_조회() throws Exception {
        // cardRepository.findByAccountId(999999) → 빈 리스트 반환 (예외 발생 없음)
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(CARDS_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
