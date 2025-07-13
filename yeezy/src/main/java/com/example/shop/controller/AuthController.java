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

            // ✅ JWT 토큰 로그 출력
            System.out.println("[login sucess] JWT Token: " + token);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 실제 예외 로그 출력
            return ResponseEntity.status(401)
                    .body(Map.of("error", "login fail: " + e.getMessage()));
        }
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
