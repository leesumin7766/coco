package com.example.shop.security;

import com.example.shop.service.UserDetailsServiceImpl;
import com.example.shop.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String[] EXCLUDE_PATTERNS = {
            "/", "/login", "/signup",
            "/api/auth/login",
            "/api/auth/signup",
            "/api/brands/**",
            "/actuator/**",
            "/css/**", "/js/**", "/images/**"
    };

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("[JwtTokenFilter] path: " + path);

        for (String pattern : EXCLUDE_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                System.out.println("[JwtTokenFilter] Excluded by pattern: " + pattern);
                filterChain.doFilter(request, response);
                return;
            }
        }
        String token = resolveToken(request);
        System.out.println("[JwtTokenFilter] Extracted token: " + token);

        if (token != null) {

            // 블랙리스트면 바로 차단 (로그아웃 토큰)
            if (jwtUtil.isBlacklisted(token)) {
                System.out.println("[JwtTokenFilter] 블랙리스트 토큰 차단");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"Logged out token\"}");
                return; //여기서 종료
            }

            // 서명/만료 검증
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[JwtTokenFilter] 인증 완료: " + userDetails.getUsername());
            }
        }

        filterChain.doFilter(request, response);
    }
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}