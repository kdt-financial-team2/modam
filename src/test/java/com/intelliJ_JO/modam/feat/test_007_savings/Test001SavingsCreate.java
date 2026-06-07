package com.intelliJ_JO.modam.feat.test_007_savings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.savings.dto.SavingsCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test-001 | 저축 목표 생성 테스트
 * 대상 API: POST /api/savings
 * 인증 불필요 (anyRequest().permitAll() 설정)
 * 화면: savings.html 에서 저축 목표 생성 폼 제출 흐름
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Test-001 | 저축 목표 생성 테스트")
class Test001SavingsCreate {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountRepository accountRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final String SAVINGS_URL = "/api/savings";

    private Account testAccount;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.save(Account.builder()
                .accountNumber("SAVACC1234567A").accountType(AccountType.GROUP).build());

        testMember = memberRepository.save(Member.builder()
                .userId("savuser001")
                .pwHash(passwordEncoder.encode("password123"))
                .name("홍길동").email("sav001@modam.com")
                .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
                .enFirst("Gildong").enLast("Hong")
                .bankName("신한은행").persAcctNo("11077345678901")
                .zipCode("06236").address("서울시 강남구 1")
                .phoneNo("01077345671")
                .rrn(passwordEncoder.encode("900101123456"))
                .account(testAccount)
                .build());
    }

    private SavingsCreateRequestDto buildDto(Long accountId, Long memberId,
                                              String goalName, String saveType,
                                              Long targetAmount, LocalDate targetDate) {
        SavingsCreateRequestDto dto = new SavingsCreateRequestDto();
        dto.setAccountId(accountId);
        dto.setMemberId(memberId);
        dto.setGoalName(goalName);
        dto.setSaveType(saveType);
        dto.setTargetAmount(targetAmount);
        dto.setTargetDate(targetDate);
        return dto;
    }

    @Test
    @DisplayName("정상: 저축 목표 생성 → 200")
    void 정상_저축_목표_생성() throws Exception {
        SavingsCreateRequestDto dto = buildDto(
                testAccount.getId(), testMember.getId(),
                "제주도 여행", "여행",
                500_000L, LocalDate.now().plusMonths(6));

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("정상: 자동이체 설정 포함 저축 목표 생성 → 200")
    void 자동이체_설정_포함_저축_목표_생성() throws Exception {
        SavingsCreateRequestDto dto = buildDto(
                testAccount.getId(), testMember.getId(),
                "결혼 자금", "결혼",
                10_000_000L, LocalDate.now().plusYears(1));
        dto.setIsAuto("Y");
        dto.setAutoAmount(500_000L);

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("목표 이름 누락 → 400 (@NotBlank)")
    void 목표이름_누락() throws Exception {
        SavingsCreateRequestDto dto = buildDto(
                testAccount.getId(), testMember.getId(),
                "", "여행",
                500_000L, LocalDate.now().plusMonths(3));

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("목표 금액 1000원 미만 → 400 (@Min(1000))")
    void 목표금액_최솟값_미만() throws Exception {
        SavingsCreateRequestDto dto = buildDto(
                testAccount.getId(), testMember.getId(),
                "용돈", "기타",
                500L, LocalDate.now().plusMonths(1));

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("목표 날짜 누락 → 400 (@NotNull)")
    void 목표날짜_누락() throws Exception {
        SavingsCreateRequestDto dto = new SavingsCreateRequestDto();
        dto.setAccountId(testAccount.getId());
        dto.setMemberId(testMember.getId());
        dto.setGoalName("테스트");
        dto.setSaveType("기타");
        dto.setTargetAmount(100_000L);
        // targetDate 누락

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 계좌 ID → 4xx/5xx")
    void 존재하지_않는_계좌() throws Exception {
        SavingsCreateRequestDto dto = buildDto(
                999999L, testMember.getId(),
                "목표", "기타",
                100_000L, LocalDate.now().plusMonths(1));

        mockMvc.perform(post(SAVINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
