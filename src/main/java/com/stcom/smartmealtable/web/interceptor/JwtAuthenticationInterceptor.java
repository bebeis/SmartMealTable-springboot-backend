package com.stcom.smartmealtable.web.interceptor;

import com.stcom.smartmealtable.security.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 인증이 필요 없는 경로 제외 (필요에 따라 설정)
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/oauth2") ||
                path.startsWith("/api/v1/auth/token/refresh")) {
            return true;
        }

        // OPTIONS 요청은 CORS preflight 요청으로 인증 필요 없음
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 토큰 존재 확인
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            return true; // 토큰이 없는 요청은 통과시키고, ArgumentResolver에서 처리
        }

        // 토큰 검증
        try {
            jwtTokenService.validateToken(token);
            return true;
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

    }

} 