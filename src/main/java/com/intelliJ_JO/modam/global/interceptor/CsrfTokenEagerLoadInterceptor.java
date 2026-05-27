package com.intelliJ_JO.modam.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * CSRF 토큰 조기 초기화 인터셉터
 *
 * 문제: HTML 파일이 클 경우(~36KB), Tomcat 기본 응답 버퍼(8KB)가 일찍 커밋되어
 *       th:action 처리 시 CSRF 세션을 새로 만들 수 없어 IllegalStateException 발생.
 *
 * 해결: preHandle 단계(뷰 렌더링 전)에서 _csrf 토큰을 강제로 resolve하여
 *       세션 생성이 응답 커밋 전에 완료되도록 보장.
 */
public class CsrfTokenEagerLoadInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // _csrf 속성은 지연(deferred) 초기화 객체로, getToken() 호출 시 세션을 생성하고 토큰을 저장한다.
        // 뷰 렌더링 전에 강제 호출하여 응답 커밋 이전에 세션이 생성되도록 한다.
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        return true;
    }
}
