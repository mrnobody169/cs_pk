package com.pokertrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartNote — Poker Cash Group Tracker API")
                        .version("1.0.0")
                        .description(
                                "REST API cho hệ thống ghi chép và theo dõi dòng tiền nhóm poker hàng ngày.\n\n" +
                                        "**Cách authenticate:**\n" +
                                        "1. Gọi `POST /api/auth/login` với email + password\n" +
                                        "2. Copy `token` từ response\n" +
                                        "3. Click nút **Authorize** (🔒) ở trên, nhập: `Bearer <token>`")
                        .contact(new Contact()
                                .name("SmartNote")
                                .email("admin@pokertrack.local")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token lấy từ endpoint POST /api/auth/login")));
    }
}
