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
import com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordUpdateRequestDto;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-003 | 소비 기록 수정 테스트
 * 대상 API: PATCH /api/spend-records/{recordId}
 * 소비 상세 화면(consumption-detail.html) 수정 버튼 흐름
 * 본인 소비 기록만 수정 가능
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 소비 기록 수정 테스트")
class Test003SpendRecordUpdate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL        = "/login";
    private static final String SPEND_RECORD_URL = "/api/spend-records";

    private Member testMember;
    private Member otherMember;
    private Account testAccount;
    private SpendRecord testSpendRecord;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("sruser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("sr003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11055345678903")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01055345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        otherMember = memberRepository.save(Member.builder()
                .userId("srother003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("srother003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33355345678903")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01055399903")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SRACC3A34567003")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(otherMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").build());

        testSpendRecord = spendRecordRepository.save(SpendRecord.builder()
                .transaction(tx)
                .title("원래 제목")
                .memo("원래 메모")
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
    @DisplayName("정상: 본인 소비 기록 제목·메모 수정 → 200")
    void 정상_소비기록_수정() throws Exception {
        MockHttpSession session = loginAs("sruser003");

        SpendRecordUpdateRequestDto dto = new SpendRecordUpdateRequestDto();
        dto.setTitle("수정된 제목");
        dto.setMemo("수정된 메모");

        mockMvc.perform(patch(SPEND_RECORD_URL + "/" + testSpendRecord.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 이모티콘만 수정 → 200 (부분 업데이트)")
    void 이모티콘만_수정() throws Exception {
        MockHttpSession session = loginAs("sruser003");

        SpendRecordUpdateRequestDto dto = new SpendRecordUpdateRequestDto();
        dto.setEmoticon("🎉");

        mockMvc.perform(patch(SPEND_RECORD_URL + "/" + testSpendRecord.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("타인 소비 기록 수정 시도 → 400 (IllegalArgumentException → GlobalExceptionHandler)")
    void 타인_소비기록_수정_시도() throws Exception {
        MockHttpSession session = loginAs("srother003");

        SpendRecordUpdateRequestDto dto = new SpendRecordUpdateRequestDto();
        dto.setTitle("타인이 바꾼 제목");

        // SpendRecordService: 소유자 확인 → IllegalArgumentException("본인의 소비 기록만 수정할 수 있습니다.") → 400
        mockMvc.perform(patch(SPEND_RECORD_URL + "/" + testSpendRecord.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("본인의 소비 기록만 수정할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 소비 기록 수정 → 4xx")
    void 존재하지_않는_소비기록_수정() throws Exception {
        MockHttpSession session = loginAs("sruser003");

        SpendRecordUpdateRequestDto dto = new SpendRecordUpdateRequestDto();
        dto.setTitle("없는 기록 수정");

        mockMvc.perform(patch(SPEND_RECORD_URL + "/999999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 소비 기록 수정 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_소비기록_수정_리다이렉트() {
        SpendRecordUpdateRequestDto dto = new SpendRecordUpdateRequestDto();
        dto.setTitle("비로그인 수정 시도");

        // SpendRecordController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        assertThrows(Exception.class, () ->
            mockMvc.perform(patch(SPEND_RECORD_URL + "/" + testSpendRecord.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))).andReturn()
        );
    }
}
