package com.stcom.smartmealtable.web.argumentresolver;

import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserContextArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenService jwtTokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserContext.class) &&
                MemberDto.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        UserContext annotation = parameter.getParameterAnnotation(UserContext.class);

        if (httpServletRequest == null) {
            throw new RuntimeException("예상치 못한 오류 발생.");
        }

        String token = httpServletRequest.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("권한 없음.");
        }

        return extractUserContext(token);
    }

    private MemberDto extractUserContext(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        var claims = jwtTokenService.extractClaims(token);

        String memberIdStr = claims.get("memberId", String.class);
        Long memberId = Long.parseLong(memberIdStr);

        // profileId 추출 (있는 경우)
        Long profileId = null;
        if (claims.containsKey("profileId")) {
            String profileIdStr = claims.get("profileId", String.class);
            profileId = Long.parseLong(profileIdStr);
        }

        // email 추출 (있는 경우)
        String email = claims.containsKey("email") ? claims.get("email", String.class) : null;

        return new MemberDto(memberId, profileId, email);
    }
}
