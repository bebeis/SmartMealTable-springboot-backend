package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;

/**
 * Controller 테스트를 위한 공통 지원 클래스.
 * 각 테스트 클래스에서 controller 인스턴스를 넘기면 MockMvc 를 만들어준다.
 */
public abstract class ControllerTestSupport {

    protected MockMvc mockMvc;
    private final HandlerMethodArgumentResolver userContextResolver = new StubUserContextResolver();

    protected void setUp(Object... controllers) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllers)
                .setCustomArgumentResolvers(userContextResolver)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    /**
     * UserContext 를 무시하고 고정된 MemberDto 를 반환하는 스텁 ArgumentResolver.
     */
    private static class StubUserContextResolver implements HandlerMethodArgumentResolver {
        private final MemberDto stub = new MemberDto(1L, 1L, "test@example.com");

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(UserContext.class) &&
                    MemberDto.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return stub;
        }
    }
} 