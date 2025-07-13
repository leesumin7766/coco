package com.example.shop.controller;

import com.example.shop.dto.OrderResponseDto;
import com.example.shop.dto.PasswordChangeRequestDto;
import com.example.shop.dto.UserResponseDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/orders")
    public List<OrderResponseDto> getOrderHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null) {
                System.out.println("❌ userDetails가 null입니다. 인증 실패로 간주됩니다.");
                throw new RuntimeException("인증 정보 없음");
            }
            System.out.println("User: " + userDetails.getUsername());
            return mypageService.getOrderHistory(userDetails.getId());
        } catch (Exception e) {
            System.out.println("🔥 Controller 내부 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/info")
    public UserResponseDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return mypageService.getUserInfo(userDetails.getId());
    }

    // controller/MypageController.java
    @PostMapping("/password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestBody PasswordChangeRequestDto requestDto) {
        mypageService.changePassword(userDetails.getId(), requestDto.getNewPassword());
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

}
