package com.example.shop.controller;

import com.example.shop.dto.LoginRequestDto;
import com.example.shop.dto.SignupRequestDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.UserService;
import com.example.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtil.createToken(userDetails.getUsername());

            // JWT 토큰 로그 출력
            System.out.println("[login sucess] JWT Token: " + token);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 실제 예외 로그 출력
            return ResponseEntity.status(401)
                    .body(Map.of("error", "login fail: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authorization header missing"));
        }

        String token = authorization.substring(7);
        System.out.println("[logout] called, token prefix=" + token.substring(0, 15));
        // 블랙리스트 등록 (Redis TTL은 exp까지 남은 시간으로)
        jwtUtil.blacklistToken(token);
        System.out.println("[logout] blacklistToken done");

        return ResponseEntity.ok(Map.of("message", "logout success"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto signupRequestDto) {
        try {
            userService.registerUser(signupRequestDto);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
