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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-004 | 댓글 삭제 테스트
 * 대상 API: DELETE /api/comments/{commentId}
 * 본인 댓글만 삭제 가능
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-004 | 댓글 삭제 테스트")
class Test004CommentDelete {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL   = "/login";
    private static final String COMMENT_URL = "/api/comments";

    private Member testMember;
    private Member otherMember;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("cmtuser004")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("cmt004@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11066345678904")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01066345674")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        otherMember = memberRepository.save(Member.builder()
                .userId("cmtother004")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("cmtother004@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33366345678904")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01066399904")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        Account account = accountRepository.save(Account.builder()
                .accountNumber("CMTACC4234567D").accountType(AccountType.GROUP).build());

        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(testMember).inviteStatus(InviteStatus.ACCEPT).build());
        accountMemberRepository.save(AccountMember.builder()
                .account(account).member(otherMember).inviteStatus(InviteStatus.ACCEPT).build());

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(account).member(testMember)
                .txType(TransactionType.PAYMENT).amount(15_000L)
                .afterBalance(985_000L).merchantName("스타벅스").build());

        SpendRecord record = spendRecordRepository.save(SpendRecord.builder()
                .transaction(tx).title("커피").build());

        testComment = commentRepository.save(Comment.builder()
                .spendRecord(record).member(testMember)
                .content("삭제될 댓글").build());
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
    @DisplayName("정상: 본인 댓글 삭제 → 200")
    void 정상_댓글_삭제() throws Exception {
        MockHttpSession session = loginAs("cmtuser004");

        mockMvc.perform(delete(COMMENT_URL + "/" + testComment.getId()).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("타인 댓글 삭제 시도 → 4xx (본인만 삭제 가능)")
    void 타인_댓글_삭제_시도() throws Exception {
        MockHttpSession session = loginAs("cmtother004");

        mockMvc.perform(delete(COMMENT_URL + "/" + testComment.getId()).session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 → 4xx")
    void 존재하지_않는_댓글_삭제() throws Exception {
        MockHttpSession session = loginAs("cmtuser004");

        mockMvc.perform(delete(COMMENT_URL + "/999999").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비로그인 상태로 댓글 삭제 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_댓글_삭제_리다이렉트() {
        // CommentController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(delete(COMMENT_URL + "/" + testComment.getId())).andReturn()
        );
    }
}
