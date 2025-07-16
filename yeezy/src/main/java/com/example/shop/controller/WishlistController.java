package com.example.shop.controller;

import com.example.shop.dto.WishlistRequestDto;
import com.example.shop.dto.WishlistResponseDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<Void> addWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody WishlistRequestDto requestDto) {
        wishlistService.addWishlist(userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponseDto>> getWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<WishlistResponseDto> wishlist = wishlistService.getWishlist(userDetails.getUser().getId());
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> deleteWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long wishlistId) {
        wishlistService.deleteWishlist(wishlistId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}


