package com.example.shop.service;

import com.example.shop.dto.BiddingRequestDto;
import com.example.shop.dto.BiddingResponseDto;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiddingService {

    private final BiddingRepository biddingRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final BiddingPositionRepository positionRepository;
    private final StatusRepository statusRepository;
    private final SizeRepository sizeRepository;

    public void createBidding(BiddingRequestDto dto, UserEntity user) {
        // 1. 상품 + 사이즈로 ProductSize 찾기
        ProductEntity product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        SizeEntity sizeEntity = sizeRepository.findByName(dto.getSize())
                .orElseThrow(() -> new IllegalArgumentException("사이즈 없음"));
        ProductSizeEntity productSize = productSizeRepository.findByProductAndSize(product, sizeEntity)
                .orElseThrow(() -> new IllegalArgumentException("해당 사이즈 없음"));

        // 2. 포지션 (SELL or BUY)
        BiddingPositionEntity position = positionRepository.findByPosition(dto.getPosition().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 포지션"));

        // 3. 상태 (ACTIVE)
        StatusEntity status = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalArgumentException("입찰 상태(PENDING)가 존재하지 않습니다."));

        // 4. 저장
        BiddingEntity bidding = new BiddingEntity();
        bidding.setUser(user);
        bidding.setProductSize(productSize);
        bidding.setPosition(position);
        bidding.setStatus(status);
        bidding.setPrice(dto.getPrice());
        bidding.setCreatedAt(LocalDateTime.now());
        bidding.setUpdatedAt(LocalDateTime.now());

        biddingRepository.save(bidding);
    }

    public List<BiddingResponseDto> getBuysByUser(UserEntity user) {
        BiddingPositionEntity buyPosition = positionRepository.findByPosition("BUY")
                .orElseThrow(() -> new IllegalArgumentException("BUY 포지션이 존재하지 않습니다."));
        List<BiddingEntity> buyBids = biddingRepository.findByUserAndPosition(user, buyPosition);
        return buyBids.stream().map(BiddingResponseDto::fromEntity).collect(Collectors.toList());
    }

    // 판매 등록 조회
    public List<BiddingResponseDto> getSalesByUser(UserEntity user) {
        // 1. SELL 포지션 찾기
        BiddingPositionEntity sellPosition = positionRepository.findByPosition("SELL")
                .orElseThrow(() -> new IllegalArgumentException("SELL 포지션이 존재하지 않습니다."));

//        StatusEntity pendingStatus = statusRepository.findByName("PENDING")
//                .orElseThrow(() -> new IllegalArgumentException("PENDING 상태가 존재하지 않습니다."));

        // 2. 해당 유저의 SELL 입찰 조회
        List<BiddingEntity> sellBiddings = biddingRepository.findByUserAndPosition(user, sellPosition);
        // 3. DTO 변환 후 반환
        return sellBiddings.stream()
                .map(BiddingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    // 입찰 취소
    public void cancelBidding(Long biddingId, UserEntity user) {
        BiddingEntity bidding = biddingRepository.findById(biddingId)
                .orElseThrow(() -> new IllegalArgumentException("입찰 내역이 존재하지 않습니다."));

        if (!bidding.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 입찰만 취소할 수 있습니다.");
        }

        if (!"PENDING".equals(bidding.getStatus().getName())) {
            throw new IllegalStateException("PENDING 상태에서만 취소할 수 있습니다.");
        }

        StatusEntity cancelledStatus = statusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new IllegalArgumentException("CANCELLED 상태가 존재하지 않습니다."));

        bidding.setStatus(cancelledStatus);
        bidding.setUpdatedAt(LocalDateTime.now());
        biddingRepository.save(bidding);
    }

}
