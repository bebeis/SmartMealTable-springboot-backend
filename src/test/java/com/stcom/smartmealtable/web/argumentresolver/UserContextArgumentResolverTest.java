package com.stcom.smartmealtable.web.argumentresolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class UserContextArgumentResolverTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private MethodParameter methodParameter;

    @InjectMocks
    private UserContextArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        lenient().when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
    }

    @Test
    @DisplayName("UserContext 어노테이션과 MemberDto 타입의 파라미터를 지원해야 한다")
    void supportsParameter() {
        // given
        when(methodParameter.hasParameterAnnotation(UserContext.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) MemberDto.class);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("UserContext 어노테이션이 없으면 파라미터를 지원하지 않아야 한다")
    void doesNotSupportParameterWithoutAnnotation() {
        // given
        when(methodParameter.hasParameterAnnotation(UserContext.class)).thenReturn(false);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("MemberDto 타입이 아니면 파라미터를 지원하지 않아야 한다")
    void doesNotSupportParameterWithWrongType() {
        // given
        when(methodParameter.hasParameterAnnotation(UserContext.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) String.class);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰으로부터 MemberDto를 추출해야 한다")
    void resolveArgumentWithValidToken() throws Exception {
        // given
        String token = "Bearer valid-token";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(token);

        Claims claims = mock(Claims.class);
        when(claims.get("memberId", String.class)).thenReturn("1");
        when(claims.get("profileId", String.class)).thenReturn("2");
        when(claims.get("email", String.class)).thenReturn("test@example.com");
        when(claims.containsKey("profileId")).thenReturn(true);
        when(claims.containsKey("email")).thenReturn(true);

        when(jwtTokenService.extractClaims(anyString())).thenReturn(claims);

        // when
        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        // then
        assertThat(result).isInstanceOf(MemberDto.class);
        MemberDto memberDto = (MemberDto) result;
        assertThat(memberDto.getMemberId()).isEqualTo(1L);
        assertThat(memberDto.getProfileId()).isEqualTo(2L);
        assertThat(memberDto.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰이 없으면 예외가 발생해야 한다")
    void resolveArgumentWithNoToken() {
        // given
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("권한 없음.");
    }

    @Test
    @DisplayName("프로필 ID가 없는 토큰으로부터 MemberDto를 추출할 수 있어야 한다")
    void resolveArgumentWithTokenWithoutProfileId() throws Exception {
        // given
        String token = "Bearer valid-token";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(token);

        Claims claims = mock(Claims.class);
        when(claims.get("memberId", String.class)).thenReturn("1");
        when(claims.get("email", String.class)).thenReturn("test@example.com");
        when(claims.containsKey("profileId")).thenReturn(false);
        when(claims.containsKey("email")).thenReturn(true);

        when(jwtTokenService.extractClaims(anyString())).thenReturn(claims);

        // when
        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        // then
        assertThat(result).isInstanceOf(MemberDto.class);
        MemberDto memberDto = (MemberDto) result;
        assertThat(memberDto.getMemberId()).isEqualTo(1L);
        assertThat(memberDto.getProfileId()).isNull();
        assertThat(memberDto.getEmail()).isEqualTo("test@example.com");
    }
} 