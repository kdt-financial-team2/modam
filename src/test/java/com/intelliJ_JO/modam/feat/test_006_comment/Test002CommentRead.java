package com.intelliJ_JO.modam.feat.test_006_comment;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.comment.entity.Comment;
import com.intelliJ_JO.modam.domain.comment.repository.CommentRepository;
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
 * Test-002 | 댓글 목록 조회 테스트 (보조 기능)
 * 대상 API: GET /api/spend-records/{recordId}/comments
 * 소비 상세 화면(consumption-detail.html) 댓글 목록 로드 시 호출
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 댓글 목록 조회 테스트")
class Test002CommentRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";

    private Member testMember;
    private SpendRecord testSpendRecord;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("cmtuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("cmt002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11066345678902")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01066345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        Account account = accountRepository.save(Account.builder()
                .accountNumber("CMTACC2234567B")
                .accountType(AccountType.GROUP)
                .build());

        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(account).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").build());

        testSpendRecord = spendRecordRepository.save(SpendRecord.builder()
                .transaction(tx).title("커피").build());

        // 댓글 2건 미리 저장
        commentRepository.save(Comment.builder()
                .spendRecord(testSpendRecord).member(testMember)
                .content("맛있어 보인다!").build());
        commentRepository.save(Comment.builder()
                .spendRecord(testSpendRecord).member(testMember)
                .content("나도 가고 싶다").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "cmtuser002")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 댓글 목록 조회 → 200 + 2건 반환")
    void 정상_댓글_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get("/api/spend-records/" + testSpendRecord.getId() + "/comments").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("댓글 없는 소비 기록 조회 → 빈 배열 반환")
    void 댓글_없는_소비기록_조회() throws Exception {
        Account emptyAcc = accountRepository.save(Account.builder()
                .accountNumber("EMPTYCMTACC001A")
                .accountType(AccountType.GROUP).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(emptyAcc).member(testMember)
                .inviteStatus(InviteStatus.ACCEPT).build());
        Transaction emptyTx = transactionRepository.save(Transaction.builder()
                .account(emptyAcc).member(testMember)
                .txType(TransactionType.PAYMENT).amount(5_000L)
                .afterBalance(995_000L).merchantName("편의점").build());
        SpendRecord emptyRecord = spendRecordRepository.save(SpendRecord.builder()
                .transaction(emptyTx).title("편의점 간식").build());

        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get("/api/spend-records/" + emptyRecord.getId() + "/comments").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("비로그인 상태로 댓글 목록 조회 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_댓글_목록_조회_리다이렉트() {
        // CommentController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(get("/api/spend-records/" + testSpendRecord.getId() + "/comments")).andReturn()
        );
    }
}
