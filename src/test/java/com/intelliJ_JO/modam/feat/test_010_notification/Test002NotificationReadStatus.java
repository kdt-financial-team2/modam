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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-002 | 알림 읽음 처리 테스트
 * 대상 API:
 *   PATCH /api/notifications/{id}/read  - 단건 읽음 처리
 *   PATCH /api/notifications/read-all   - 전체 읽음 처리
 * @AuthenticationPrincipal 사용 → 인증 필요
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-002 | 알림 읽음 처리 테스트")
class Test002NotificationReadStatus {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/login";
    private static final String NOTI_URL  = "/api/notifications";

    private Member testMember;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .userId("notiuser002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("noti002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11011345678902")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01011345672")
                .rrn(passwordEncoder.encode("900101123456"))
                .build());

        memberRepository.save(Member.builder()
                .userId("notiother002")
                .pwHash(passwordEncoder.encode("password123"))
                .name("다른회원").email("notiother002@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Other").enLast("Mem")
                .bankName("카카오뱅크").persAcctNo("33011345678902")
                .zipCode("06001").address("서울 1")
                .phoneNo("01011399902")
                .rrn(passwordEncoder.encode("950101123456"))
                .build());

        // 테스트 회원의 미읽음 알림 2건
        testNotification = notificationRepository.save(Notification.builder()
                .member(testMember).notiType(NotificationType.DEPOSIT)
                .message("5,000원이 입금되었습니다.").targetUrl("/account").build());
        notificationRepository.save(Notification.builder()
                .member(testMember).notiType(NotificationType.SAVINGS_GOAL)
                .message("저축 목표 달성!").targetUrl("/savings").build());
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
    @DisplayName("정상: 단건 알림 읽음 처리 → 200 + isRead Y")
    void 정상_단건_읽음_처리() throws Exception {
        MockHttpSession session = loginAs("notiuser002");

        mockMvc.perform(patch(NOTI_URL + "/" + testNotification.getId() + "/read")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").value("Y"));
    }

    @Test
    @DisplayName("타인 알림 읽음 처리 시도 → 4xx/5xx (본인 알림만 처리 가능)")
    void 타인_알림_읽음_처리() throws Exception {
        MockHttpSession session = loginAs("notiother002");

        mockMvc.perform(patch(NOTI_URL + "/" + testNotification.getId() + "/read")
                        .session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 → 4xx")
    void 존재하지_않는_알림_읽음() throws Exception {
        MockHttpSession session = loginAs("notiuser002");

        mockMvc.perform(patch(NOTI_URL + "/999999/read").session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("정상: 전체 알림 읽음 처리 → 200")
    void 정상_전체_읽음_처리() throws Exception {
        MockHttpSession session = loginAs("notiuser002");

        mockMvc.perform(patch(NOTI_URL + "/read-all").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
