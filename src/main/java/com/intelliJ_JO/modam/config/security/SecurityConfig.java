package com.intelliJ_JO.modam.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 전역 보안 설정 클래스
 * - 인증(Authentication): 사용자가 누구인지 확인 (로그인)
 * - 인가(Authorization): 인증된 사용자가 어떤 자원에 접근할 수 있는지 제어
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // DB에서 사용자 정보를 조회하는 서비스 (UserDetails 구현체)
    private final CustomUserDetailsService customUserDetailsService;

    // 로그인 성공 시 실행되는 핸들러 (예: 역할에 따른 페이지 분기)
    private final CustomAuthenticationSuccessHandler successHandler;

    // 로그인 실패 시 실행되는 핸들러 (예: 에러 메시지 처리)
    private final CustomAuthenticationFailureHandler failureHandler;

    // 비밀번호 암호화에 사용되는 BCrypt 인코더 (PasswordEncoderConfig에서 Bean 등록)
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * DB 기반 인증 프로바이더 설정
     * - CustomUserDetailsService로 사용자 정보를 조회하고
     * - BCryptPasswordEncoder로 비밀번호를 검증한다
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * AuthenticationManager Bean 등록
     * - 직접 인증 처리가 필요한 경우 (예: REST API 로그인) 주입받아 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * HTTP 보안 필터 체인 설정
     * - 접근 권한, CSRF, 로그인/로그아웃 동작을 정의한다
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 위에서 설정한 DaoAuthenticationProvider를 Security에 등록
            .authenticationProvider(authenticationProvider())

            // 같은 출처(Same-Origin)의 iframe 허용 (H2 콘솔 등 사용 시 필요)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // H2 콘솔과 REST API 경로는 CSRF 토큰 검증에서 제외
            // (H2 콘솔은 폼 기반, API는 stateless 방식으로 CSRF 불필요)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))

            // 요청별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // 로그인·회원가입 관련 경로 (비로그인 접근 허용)
                    "/", "/landing", "/auth/login", "/auth/login?*", "/login",
                    "/signup/**", "/terms",
                    "/members/join",
                    // 정적 리소스 (CSS, JS, 이미지)
                    "/css/**", "/js/**", "/images/**",
                    // Swagger API 문서 경로
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/v3/api-docs", "/v3/api-docs/**",
                    // H2 콘솔 (개발용 DB 관리 도구)
                    "/h2-console/**"
                ).permitAll()                   // 위 경로는 인증 없이 접근 허용
                .requestMatchers("/dashboard/**").authenticated() // 대시보드는 인증 필요
                .anyRequest().permitAll()       // 나머지 요청은 인증 없이 접근 허용
            )

            // 폼 로그인 설정
            .formLogin(form -> form
                .loginPage("/auth/login")               // 커스텀 로그인 페이지 경로
                .loginProcessingUrl("/login")           // 로그인 폼 submit POST 경로
                .successHandler(successHandler)         // 로그인 성공 시 커스텀 핸들러 실행
                .failureHandler(failureHandler)         // 로그인 실패 시 커스텀 핸들러 실행
                .usernameParameter("userId")            // 로그인 폼의 아이디 필드명
                .passwordParameter("password")          // 로그인 폼의 비밀번호 필드명
                .permitAll()                            // 로그인 페이지 자체는 모두 허용
            )

            // 자동 로그인(Remember-Me) 설정
            .rememberMe(rememberMe -> rememberMe
                .userDetailsService(customUserDetailsService)
                .key("modam-remember-me-secret-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                .rememberMeParameter("remember-me")
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")                   // 로그아웃 요청 경로 (POST)
                .logoutSuccessUrl("/auth/login")        // 로그아웃 후 이동할 페이지
                .invalidateHttpSession(true)            // 세션 무효화
                .deleteCookies("JSESSIONID", "remember-me") // 세션 및 자동 로그인 쿠키 삭제
            );

        return http.build();
    }
}
