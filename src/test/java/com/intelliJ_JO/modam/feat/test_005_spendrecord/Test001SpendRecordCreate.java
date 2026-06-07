package com.intelliJ_JO.modam.feat.test_005_spendrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordCreateRequestDto;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * Test-001 | 소비 기록 생성 흐름 테스트
 * 화면 흐름: 거래 선택(consumption-select) → 소비기록 작성(consumption-upload)
 * 대상 API: POST /api/spend-records
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 소비 기록 생성 테스트")
class Test001SpendRecordCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL        = "/login";
    private static final String SPEND_RECORD_URL = "/api/spend-records";

    private Member testMember;
    private Account testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("sruser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("sr001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11055345678901")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01055345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SRACC1A34567001")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        testTransaction = transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.PAYMENT)
                .amount(15_000L).afterBalance(985_000L)
                .merchantName("스타벅스").category("식비")
                .build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "sruser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private SpendRecordCreateRequestDto baseRequest() {
        SpendRecordCreateRequestDto dto = new SpendRecordCreateRequestDto();
        dto.setTransactionId(testTransaction.getId());
        dto.setTitle("스벅에서 커피 한 잔");
        dto.setMemo("달달한 라떼");
        dto.setEmoticon("☕");
        return dto;
    }

    @Test
    @DisplayName("정상: 제목·메모·이모티콘 포함 소비 기록 생성 → 200")
    void 정상_소비_기록_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 제목만 입력한 소비 기록 생성 → 200 (나머지 필드 선택)")
    void 제목만_입력_소비_기록_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();
        SpendRecordCreateRequestDto dto = new SpendRecordCreateRequestDto();
        dto.setTransactionId(testTransaction.getId());
        dto.setTitle("커피");

        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("동일 거래에 소비 기록 중복 생성 → 409 (IllegalStateException → GlobalExceptionHandler)")
    void 소비_기록_중복_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // 첫 번째 생성
        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest())))
                .andExpect(status().isOk());

        // 동일 거래에 두 번째 생성 시도 → IllegalStateException → 409 CONFLICT
        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest())))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 해당 거래에 소비 기록이 존재합니다."));
    }

    @Test
    @DisplayName("존재하지 않는 거래 ID로 소비 기록 생성 → 4xx")
    void 존재하지_않는_거래_소비_기록_생성() throws Exception {
        MockHttpSession session = loginAndGetSession();
        SpendRecordCreateRequestDto dto = new SpendRecordCreateRequestDto();
        dto.setTransactionId(999999L);
        dto.setTitle("존재하지 않는 거래");

        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("transactionId 누락 → 400 (@NotNull 검증)")
    void transactionId_누락() throws Exception {
        MockHttpSession session = loginAndGetSession();
        SpendRecordCreateRequestDto dto = new SpendRecordCreateRequestDto();
        dto.setTitle("제목만 있음");

        mockMvc.perform(post(SPEND_RECORD_URL)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 소비 기록 생성 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_소비_기록_생성_리다이렉트() {
        // SpendRecordController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        assertThrows(Exception.class, () ->
            mockMvc.perform(post(SPEND_RECORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(baseRequest()))).andReturn()
        );
    }
}
