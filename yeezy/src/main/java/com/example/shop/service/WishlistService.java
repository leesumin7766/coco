package com.example.shop.service;

import com.example.shop.dto.WishlistRequestDto;
import com.example.shop.dto.WishlistResponseDto;
import com.example.shop.entity.ProductEntity;
import com.example.shop.entity.UserEntity;
import com.example.shop.entity.WishlistEntity;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import com.example.shop.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addWishlist(Long userId, WishlistRequestDto requestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        ProductEntity product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        WishlistEntity wishlist = new WishlistEntity();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setSize(requestDto.getSize());
        wishlistRepository.save(wishlist);
    }

    public List<WishlistResponseDto> getWishlist(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return wishlistRepository.findAllByUser(user).stream()
                .map(WishlistResponseDto::new)
                .toList();
    }

    @Transactional
    public void deleteWishlist(Long wishlistId, Long userId) {
        WishlistEntity wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 항목"));
        if (!wishlist.getUser().getId().equals(userId)) {
            throw new SecurityException("권한 없음");
        }
        wishlistRepository.delete(wishlist);
    }
}

