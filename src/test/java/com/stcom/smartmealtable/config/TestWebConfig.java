package com.stcom.smartmealtable.config;

import com.stcom.smartmealtable.security.JwtTokenService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestWebConfig {

    @Bean
    @Primary
    public JwtTokenService jwtTokenService() {
        return Mockito.mock(JwtTokenService.class);
    }
} 