// com.example.shop.config.CorsConfig.java
package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")                       // 모든 경로에 대해
                .allowedOrigins(
                        "http://localhost:*",           // 로컬 개발
//                        "http://3.38.108.76:3000",
//                        "http://3.38.108.76:80",
//                        "http://3.38.108.76:22", // 실제 배포된 프론트
                        "http://3.37.52.16",        // Nginx 80 (포트 표기 없이)
                        "http://3.37.52.16:3000"    // dev server를 이런 식으로 띄운다면
                        // "https://your-domain.com" // HTTPS 사용 시 추가
                )
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization","Location")
                .allowCredentials(true);
    }
}