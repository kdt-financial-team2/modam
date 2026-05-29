package com.intelliJ_JO.modam.config;

import com.intelliJ_JO.modam.global.interceptor.CsrfTokenEagerLoadInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring MVC 추가 설정 클래스
 * - 인터셉터 등록 등 MVC 확장 설정을 관리한다
 * - 프로필 이미지 등 외부 정적 리소스 경로를 매핑한다
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 기존 설정: 모든 뷰 요청에 대해 CSRF 토큰을 응답 커밋 전에 미리 초기화한다
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CsrfTokenEagerLoadInterceptor());
    }

    // 🔥 추가된 설정: 로컬 파일 시스템의 uploads 폴더를 웹 경로(/uploads/**)로 매핑한다
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 운영체제 상관없이 현재 프로젝트의 최상단 루트(Root) 절대 경로를 추출 (Windows, Mac 자동 호환)
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, "uploads");

        // 시스템 파일 시스템 경로를 스프링 리소스 위치 포맷(file:/...)으로 안전하게 전환
        String uploadAbsolutePath = uploadPath.toAbsolutePath().toUri().toString();

        // 브라우저가 /uploads/** 경로로 요청 시, 로컬 프로젝트 루트/uploads/ 하위의 파일을 리턴하도록 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);
    }
}