package com.example.shop.controller;

import com.example.shop.dto.*;
import com.example.shop.entity.TradeEntity;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final ProductService productService;
    private final WishlistService wishlistService;
    private final TradeService tradeService;
    private final BiddingService biddingService;

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

    @GetMapping("/wishlist")
    public List<WishlistResponseDto> getWishlist(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return wishlistService.getWishlist(userDetails.getUser().getId());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getUserProducts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        List<ProductResponseDto> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trades")
    public List<TradeResponseDto> getUserTrades(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TradeEntity> trades = tradeService.getUserTrades(userDetails.getUser());
        return trades.stream()
                .map(trade -> TradeResponseDto.fromEntity(trade, userDetails.getUser()))
                .collect(Collectors.toList());
    }

    // ✅ 추가: 내가 등록한 구매 입찰 조회
    @GetMapping("/biddings/buys")
    public List<BiddingResponseDto> getMyBuys(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return biddingService.getBuysByUser(userDetails.getUser());
    }

    // ✅ 추가: 내가 등록한 판매 입찰 조회
    @GetMapping("/biddings/sales")
    public List<BiddingResponseDto> getMySales(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return biddingService.getSalesByUser(userDetails.getUser());
    }

}
