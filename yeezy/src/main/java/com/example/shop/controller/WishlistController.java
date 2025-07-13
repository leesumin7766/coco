package com.example.shop.controller;

import com.example.shop.dto.WishlistRequestDto;
import com.example.shop.dto.WishlistResponseDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public void addWishlist(@AuthenticationPrincipal UserDetailsImpl userDetails,
                            @RequestBody WishlistRequestDto requestDto) {
        wishlistService.addWishlist(userDetails.getUser().getId(), requestDto);
    }

    @GetMapping
    public List<WishlistResponseDto> getWishlist(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return wishlistService.getWishlist(userDetails.getUser().getId());
    }

    @DeleteMapping("/{wishlistId}")
    public void deleteWishlist(@PathVariable Long wishlistId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        wishlistService.deleteWishlist(wishlistId, userDetails.getUser().getId());
    }
}

