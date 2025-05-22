package com.stcom.smartmealtable.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@Profile({"local","dev"})
@OpenAPIDefinition(
    info = @Info(
        title = "SmartMealTable API 문서",
        version = "v1",
        description = "SmartMealTable API 명세서입니다."
    )
)
public class SwaggerConfig {
} 