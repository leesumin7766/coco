package com.example.shop.service;

import com.example.shop.dto.WishlistRequestDto;
import com.example.shop.dto.WishlistResponseDto;
import com.example.shop.entity.UserEntity;
import com.example.shop.entity.WishlistEntity;
import com.example.shop.repository.UserRepository;
import com.example.shop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    public void addWishlist(Long userId, WishlistRequestDto requestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        WishlistEntity wishlist = new WishlistEntity(requestDto.getProductName(), requestDto.getPrice(), user);
        wishlistRepository.save(wishlist);
    }

    public List<WishlistResponseDto> getWishlist(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return wishlistRepository.findAllByUser(user).stream()
                .map(WishlistResponseDto::new)
                .collect(Collectors.toList());
    }

    public void deleteWishlist(Long wishlistId, Long userId) {
        WishlistEntity wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new IllegalArgumentException("해당 위시리스트 항목이 없습니다."));

        if (!wishlist.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        wishlistRepository.delete(wishlist);
    }
}
