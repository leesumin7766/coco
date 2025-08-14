// src/main/java/com/example/shop/config/WebConfig.java
package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 다른 MVC 설정이 필요하다면 여기에 작성 (정적 리소스, 포맷터 등)
    // 단, CORS는 CorsConfig + Security에서만!
}
