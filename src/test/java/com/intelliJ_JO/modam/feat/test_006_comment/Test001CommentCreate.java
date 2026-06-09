package com.intelliJ_JO.modam.feat.test_006_comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentCreateRequestDto;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.transaction.entity.Transaction;
import com.intelliJ_JO.modam.domain.transaction.entity.TransactionType;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
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
 * Test-001 | 댓글 작성 흐름 테스트
 * 대상 API: POST /api/spend-records/{recordId}/comments
 * 소비 상세 화면(consumption-detail.html) 댓글 작성 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 댓글 작성 테스트")
class Test001CommentCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";

    private Member testMember;
    private Member outsider;
    private Account testAccount;
    private SpendRecord testSpendRecord;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("cmtuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("cmt001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11066345678901")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01066345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        outsider = memberRepository.save(Member.builder()
                .userId("out001cmt")
                .pwHash(passwordEncoder.encode("password123"))
                .name("외부인").email("out001cmt@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Out").enLast("Sider")
                .bankName("국민은행").persAcctNo("99966345678901")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01099945671")
                .rrn(passwordEncoder.encode("800101123456"))
                .build());

        testAccount = accountRepository.save(Account.builder()
                .accountNumber("CMTACC1234567A")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(testAccount).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        // outsider는 계좌 구성원이 아님

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(testAccount).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").build());

        testSpendRecord = spendRecordRepository.save(SpendRecord.builder()
                .transaction(tx).title("스벅 커피").build());
    }

    private MockHttpSession loginAs(String userId) throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", userId)
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String commentUrl() {
        return "/api/spend-records/" + testSpendRecord.getId() + "/comments";
    }

    @Test
    @DisplayName("정상: 계좌 구성원이 댓글 작성 → 200")
    void 정상_댓글_작성() throws Exception {
        MockHttpSession session = loginAs("cmtuser001");

        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("맛있어 보인다!");

        mockMvc.perform(post(commentUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 이모티콘 포함 댓글 작성 → 200")
    void 이모티콘_포함_댓글_작성() throws Exception {
        MockHttpSession session = loginAs("cmtuser001");

        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("부럽다!");
        dto.setEmoticon("😍");

        mockMvc.perform(post(commentUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("댓글 내용 누락(공백) → 400 (@NotBlank)")
    void 댓글_내용_누락() throws Exception {
        MockHttpSession session = loginAs("cmtuser001");

        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("");

        mockMvc.perform(post(commentUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("댓글 내용 500자 초과 → 400 (@Size(max=500))")
    void 댓글_내용_500자_초과() throws Exception {
        MockHttpSession session = loginAs("cmtuser001");

        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("가".repeat(501)); // 501자

        mockMvc.perform(post(commentUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("계좌 구성원이 아닌 외부인이 댓글 작성 → 4xx")
    void 외부인_댓글_작성_시도() throws Exception {
        MockHttpSession session = loginAs("out001cmt");

        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("외부인 댓글");

        mockMvc.perform(post(commentUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 댓글 작성 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_댓글_작성_리다이렉트() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setContent("비로그인 댓글");

        // CommentController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(post(commentUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))).andReturn()
        );
    }
}
