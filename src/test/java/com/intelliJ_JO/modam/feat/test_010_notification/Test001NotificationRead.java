package com.intelliJ_JO.modam.feat.test_010_notification;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.notification.entity.Notification;
import com.intelliJ_JO.modam.domain.notification.entity.NotificationType;
import com.intelliJ_JO.modam.domain.notification.repository.NotificationRepository;
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
 * Test-001 | 알림 목록 및 미읽음 수 조회 테스트 (보조 기능)
 * 대상 API:
 *   GET /api/notifications              - 알림 목록 (커서 기반 페이지네이션)
 *   GET /api/notifications/unread-count - 미읽음 알림 수
 * @AuthenticationPrincipal 사용 → 인증 필요
 * SSE 구독(/subscribe)은 MockMvc 테스트 제외 (SSE 스트림 방식)
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 알림 목록 조회 테스트")
class Test001NotificationRead {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String NOTI_URL  = "/api/notifications";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("notiuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("noti001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11011345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01011345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        // 알림 3건 저장 (읽음 1건, 미읽음 2건)
        notificationRepository.save(Notification.builder()
                .member(testMember).notiType(NotificationType.DEPOSIT)
                .message("10,000원이 입금되었습니다.").targetUrl("/account").isRead("Y").build());
        notificationRepository.save(Notification.builder()
                .member(testMember).notiType(NotificationType.INVITE)
                .message("모임통장에 초대되었습니다.").targetUrl("/invites/1").build());
        notificationRepository.save(Notification.builder()
                .member(testMember).notiType(NotificationType.POINT_SPEND)
                .message("100P를 사용했습니다.").targetUrl("/point-shop").build());
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "notiuser001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    @DisplayName("정상: 알림 목록 조회 (첫 페이지) → 200 + 3건 반환")
    void 정상_알림_목록_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(NOTI_URL).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션: lastId 파라미터 포함 조회 → 200")
    void 커서_기반_페이지네이션() throws Exception {
        MockHttpSession session = loginAndGetSession();

        // lastId를 1로 설정하면 id > 1 인 데이터만 조회
        mockMvc.perform(get(NOTI_URL)
                        .session(session)
                        .param("lastId", "1")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("미읽음 알림 수 조회 → 200 + 2 반환")
    void 미읽음_알림_수_조회() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(get(NOTI_URL + "/unread-count").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("알림 없는 회원의 미읽음 수 조회 → 0 반환")
    void 알림없는_회원_미읽음_수() throws Exception {
        memberRepository.save(Member.builder()
                .userId("notiempty001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("빈회원").email("empty001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Empty").enLast("User")
                .bankName("카카오뱅크").persAcctNo("33011345678901")
                .zipCode("06001").address("서울 1")
                .phoneNo("01011300001")
                .rrn(passwordEncoder.encode("920101123456"))
                .build());

        MvcResult result = mockMvc.perform(formLogin(LOGIN_URL)
                        .user("userId", "notiempty001")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession emptySession = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get(NOTI_URL + "/unread-count").session(emptySession))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }
}
