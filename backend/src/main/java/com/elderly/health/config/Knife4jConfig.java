package com.elderly.health.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Knife4j (OpenAPI3) 接口文档配置类
 *
 * @author elderly-health
 */
@Configuration
public class Knife4jConfig {

    private static final String BEARER_AUTH = "Bearer Token";

    /**
     * 配置 OpenAPI 文档信息
     * 包含文档标题、版本、描述、服务器地址、Bearer Token 认证
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        // 文档信息
        Info info = new Info()
                .title("社区老年人健康管理服务平台 API")
                .version("1.0.0")
                .description("社区老年人健康管理服务平台接口文档")
                .contact(new Contact()
                        .name("elderly-health")
                        .email("admin@elderly-health.com"));

        // 服务器地址
        Server server = new Server()
                .url("http://localhost:8080")
                .description("本地开发环境");

        // Bearer Token 认证方案
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("请输入 Bearer Token 进行认证，格式：Bearer {token}");

        // 全局认证要求
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(BEARER_AUTH);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .schemaRequirement(BEARER_AUTH, securityScheme)
                .security(Collections.singletonList(securityRequirement));
    }
}
