// src/main/java/com/example/shop/config/CorsConfig.java
package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // 자격증명(쿠키/세션/JWT-쿠키) 사용 시: allowedOrigins("*") 금지
        // 80 포트는 ':80' 없이 "http://IP"가 오리진입니다.
        c.setAllowedOriginPatterns(List.of(
                "http://3.37.52.16",   // Nginx 80 (포트 표기 없음)
                "http://3.37.52.16:*", // (선택) dev 포트(예: 3000)
                "http://localhost:*"   // 로컬 개발
                // "https://your-domain.com" // 도메인/HTTPS 있다면 추가
        ));

        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Authorization", "Location"));
        c.setAllowCredentials(true); // 쿠키/세션/JWT-쿠키 쓸 때 필요

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
