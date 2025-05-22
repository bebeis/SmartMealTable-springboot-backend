package com.stcom.smartmealtable.config;

import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;

@Configuration
@Profile({"local", "dev"})
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth"),
        info = @Info(
                title = "SmartMealTable API 문서",
                version = "v1",
                description = "SmartMealTable API 명세서입니다."
        )
)
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(UserContext.class);
    }

    @Bean
    public OperationCustomizer userContextSecurityCustomizer() {
        return (operation, handlerMethod) -> {
            for (MethodParameter param : handlerMethod.getMethodParameters()) {
                if (param.hasParameterAnnotation(UserContext.class)) {
                    operation.addSecurityItem(
                            new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"));
                    break;
                }
            }
            return operation;
        };
    }
} 