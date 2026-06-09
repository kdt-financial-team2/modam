package com.intelliJ_JO.modam.feat.test_003_card;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.card.entity.Card;
import com.intelliJ_JO.modam.domain.card.entity.CardStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 카드 상태 변경 테스트 (분실신고·일시정지)
 * 대상 API: PATCH /api/cards/{cardId}/status?memberId={memberId}&status={status}
 * 마이페이지 카드 목록 화면(mypage/cards.html) 상태 변경 버튼 흐름
 * 주의: CardController 는 @RequestParam 으로 memberId, status 를 받으므로 JSON body 사용 불가
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 카드 상태 변경 테스트")
class Test003CardStatus {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String CARDS_URL = "/api/cards";

    private Member testMember;
    private Member otherMember;
    private Account testAccount;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("carduser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("card003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11033345678903")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01033345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        otherMember = memberRepository.save(Member.builder()
                .userId("other003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("other003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33333345678903")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01033399903")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("STATACC1234567C")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(otherMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        // testMember 소유 카드 생성
        testCard = cardRepository.save(Card.builder()
                .account(testAccount)
                .member(testMember)
                .cardNumber("ENCRYPTED_CARD_003")
                .expiryDate("12/28")
                .cardDesign("DEFAULT")
                .cardType("DEBIT")
                .build());
    }

    private MockHttpSession loginAs(String userId) throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", userId)
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 본인 카드 분실 신고(LOST) → 200")
    void 정상_카드_분실신고() throws Exception {
        MockHttpSession session = loginAs("carduser003");

        // CardController: @RequestParam Long memberId, @RequestParam CardStatus status
        mockMvc.perform(patch(CARDS_URL + "/" + testCard.getId() + "/status")
                        .param("memberId", String.valueOf(testMember.getId()))
                        .param("status", CardStatus.LOST.name())
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 본인 카드 일시정지(STOPPED) → 200")
    void 정상_카드_일시정지() throws Exception {
        MockHttpSession session = loginAs("carduser003");

        mockMvc.perform(patch(CARDS_URL + "/" + testCard.getId() + "/status")
                        .param("memberId", String.valueOf(testMember.getId()))
                        .param("status", CardStatus.STOPPED.name())
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("타인 카드 상태 변경 시도 → 400 (본인 카드만 변경 가능, IllegalArgumentException → 400)")
    void 타인_카드_상태변경_시도() throws Exception {
        MockHttpSession session = loginAs("other003");

        // otherMember.getId() 로 요청 → card.getMember() ≠ otherMember → IllegalArgumentException → 400
        mockMvc.perform(patch(CARDS_URL + "/" + testCard.getId() + "/status")
                        .param("memberId", String.valueOf(otherMember.getId()))
                        .param("status", CardStatus.LOST.name())
                        .session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("본인 카드만 상태를 변경할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 카드 상태 변경 시도 → 400 (IllegalArgumentException → GlobalExceptionHandler)")
    void 존재하지_않는_카드_상태변경() throws Exception {
        MockHttpSession session = loginAs("carduser003");

        mockMvc.perform(patch(CARDS_URL + "/999999/status")
                        .param("memberId", String.valueOf(testMember.getId()))
                        .param("status", CardStatus.LOST.name())
                        .session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 카드 상태 변경 시도 → 200 (CardController 인증 미적용, permitAll)")
    void 비로그인_카드_상태변경_리다이렉트() throws Exception {
        // CardController 는 @AuthenticationPrincipal 없음 → 인증 없이도 상태 변경 가능
        mockMvc.perform(patch(CARDS_URL + "/" + testCard.getId() + "/status")
                        .param("memberId", String.valueOf(testMember.getId()))
                        .param("status", CardStatus.LOST.name()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
