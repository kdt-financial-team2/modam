package com.intelliJ_JO.modam.config;

import com.intelliJ_JO.modam.global.interceptor.CsrfTokenEagerLoadInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 추가 설정 클래스
 * - 인터셉터 등록 등 MVC 확장 설정을 관리한다
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 뷰 요청에 대해 CSRF 토큰을 응답 커밋 전에 미리 초기화한다
        registry.addInterceptor(new CsrfTokenEagerLoadInterceptor());
    }
}
