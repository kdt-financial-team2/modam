package com.intelliJ_JO.modam.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080/api"))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Couple Finance API")
                .description("커플 공동 금융 서비스 API 문서")
                .version("1.0.0");
    }
}