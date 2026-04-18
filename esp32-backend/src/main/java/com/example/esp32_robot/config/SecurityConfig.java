package com.example.esp32_robot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // ✅ 禁用 CSRF（API 通常不需要）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()  // ✅ 放行登录和注册
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // ✅ 放行 Swagger
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())  // 禁用表单登录
                .httpBasic(basic -> basic.disable());  // 禁用 HTTP Basic

        return http.build();
    }
}