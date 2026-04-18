package com.example.esp32_robot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ESP32 机器人后端 API")
                        .version("1.0.0")
                        .description("ESP32 智能机器人后端服务 API 文档"));
    }
}