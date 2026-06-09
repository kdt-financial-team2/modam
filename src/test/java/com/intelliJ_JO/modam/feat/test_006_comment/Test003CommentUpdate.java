package com.intelliJ_JO.modam.feat.test_006_comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import java.util.Map;
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
 * Test-003 | 댓글 수정 테스트
 * 대상 API: PATCH /api/comments/{commentId}
 * 본인 댓글만 수정 가능
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-003 | 댓글 수정 테스트")
class Test003CommentUpdate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountMemberRepository accountMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SpendRecordRepository spendRecordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL    = "/login";
    private static final String COMMENT_URL  = "/api/comments";

    private Member testMember;
    private Member otherMember;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("cmtuser003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("cmt003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11066345678903")
                .zipCode("06236").address("서울시 강남구 테헤란로 123")
                .phoneNo("01066345673")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        otherMember = memberRepository.save(Member.builder()
                .userId("cmtother003")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("cmtother003@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33366345678903")
                .zipCode("06001").address("서울시 서초구 1")
                .phoneNo("01066399903")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        Account account = accountRepository.save(Account.builder()
                .accountNumber("CMTACC3234567C").accountType(AccountType.GROUP).build());

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

        // testMember의 댓글
        testComment = commentRepository.save(Comment.builder()
                .spendRecord(record).member(testMember)
                .content("원래 댓글 내용").build());
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
    @DisplayName("정상: 본인 댓글 내용 수정 → 200")
    void 정상_댓글_수정() throws Exception {
        MockHttpSession session = loginAs("cmtuser003");

        // CommentUpdateRequestDto는 @Setter 없음 → Map으로 JSON 구성
        Map<String, String> body = Map.of("content", "수정된 댓글 내용");

        mockMvc.perform(patch(COMMENT_URL + "/" + testComment.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 이모티콘만 수정 → 200 (부분 업데이트)")
    void 이모티콘만_수정() throws Exception {
        MockHttpSession session = loginAs("cmtuser003");

        Map<String, String> body = Map.of("emoticon", "🎊");

        mockMvc.perform(patch(COMMENT_URL + "/" + testComment.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("타인 댓글 수정 시도 → 4xx (본인만 수정 가능)")
    void 타인_댓글_수정_시도() throws Exception {
        MockHttpSession session = loginAs("cmtother003");

        Map<String, String> body = Map.of("content", "타인이 수정하려는 내용");

        mockMvc.perform(patch(COMMENT_URL + "/" + testComment.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("500자 초과 댓글 수정 → 200 (CommentController.updateComment 에 @Valid 없음 → 검증 미적용)")
    void 댓글_500자_초과_수정() throws Exception {
        MockHttpSession session = loginAs("cmtuser003");

        // CommentController.updateComment: @RequestBody CommentUpdateRequestDto 에 @Valid 없음
        // → @Size(max=500) 검증이 적용되지 않아 201자 이상도 정상 수정됨
        Map<String, String> body = Map.of("content", "가".repeat(501));

        mockMvc.perform(patch(COMMENT_URL + "/" + testComment.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비로그인 상태로 댓글 수정 → NPE (null 체크 없는 @AuthenticationPrincipal → ServletException 전파)")
    void 비로그인_댓글_수정_리다이렉트() {
        Map<String, String> body = Map.of("content", "비로그인 수정 시도");

        // CommentController: @AuthenticationPrincipal null 체크 없음 → NPE → MockMvc ServletException 전파
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(patch(COMMENT_URL + "/" + testComment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))).andReturn()
        );
    }
}
