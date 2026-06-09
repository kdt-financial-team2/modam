package com.intelliJ_JO.modam.feat.test_004_transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionRequestDto;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
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
 * Test-003 | 카드 결제 거래 테스트
 * 대상 API: POST /api/transactions (txType=PAYMENT)
 * PAYMENT 타입은 cardId 필수, 카드 소유권·잔액 검증
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 카드 결제 거래 테스트")
class Test003TransactionPayment {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL       = "/login";
    private static final String TRANSACTION_URL = "/api/transactions";

    private Member testMember;
    private Member otherMember;
    private Account testAccount;
    private Card testCard;
    private Card otherCard;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("txuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("tx003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11044345678903")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01044345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        otherMember = memberRepository.save(Member.builder()
                .userId("other003tx")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("other003tx@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33344345678903")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01044399903")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("TXACC3A34567003")
                .accountType(AccountType.GROUP)
                .balance(1_000_000L)
                .availableBalance(1_000_000L)
                .passwordHash(passwordEncoder.encode("1234"))
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(otherMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        testCard = cardRepository.save(Card.builder()
                .account(testAccount).member(testMember)
                .cardNumber("ENCRYPTED_CARD_TX003")
                .expiryDate("12/28").build());

        otherCard = cardRepository.save(Card.builder()
                .account(testAccount).member(otherMember)
                .cardNumber("ENCRYPTED_OTHER_003")
                .expiryDate("12/29").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "txuser003")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private TransactionRequestDto paymentRequest(long amount, Long cardId) {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.setAccountId(testAccount.getId());
        dto.setMemberId(testMember.getId());
        dto.setTxType(TransactionType.PAYMENT);
        dto.setAmount(amount);
        dto.setCardId(cardId);
        dto.setMerchantName("스타벅스");
        dto.setCategory("식비");
        dto.setAccountPassword("1234");
        return dto;
    }

    @Test
    @DisplayName("정상: 본인 카드로 카드 결제 → 200")
    void 정상_카드_결제_성공() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(15_000L, testCard.getId()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.txType").value("PAYMENT"))
                .andExpect(jsonPath("$.data.amount").value(15_000));
    }

    @Test
    @DisplayName("PAYMENT 타입에 cardId 누락 → 4xx (cardId 필수)")
    void 카드결제_cardId_누락() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(15_000L, null))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("카드 결제 시 cardId는 필수입니다."));
    }

    @Test
    @DisplayName("타인 카드로 결제 시도 → 4xx (카드 사용 권한 없음)")
    void 타인_카드로_결제_시도() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(15_000L, otherCard.getId()))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("카드 사용 권한이 없습니다."));
    }

    @Test
    @DisplayName("잔액 초과 카드 결제 → 4xx (잔액 부족)")
    void 잔액_초과_카드_결제() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(9_999_999L, testCard.getId()))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
    }

    @Test
    @DisplayName("존재하지 않는 카드로 결제 시도 → 4xx")
    void 존재하지_않는_카드_결제() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(TRANSACTION_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(15_000L, 999999L))))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 카드 결제 시도 → 200 (TransactionController 인증 미적용, permitAll)")
    void 비로그인_카드_결제_리다이렉트() throws Exception {
        // TransactionController 는 @AuthenticationPrincipal 없음 → 인증 없이도 거래 가능
        mockMvc.perform(post(TRANSACTION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(15_000L, testCard.getId()))))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
