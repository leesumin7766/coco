package com.example.shop.service;

import com.example.shop.dto.BiddingRequestDto;
import com.example.shop.dto.BiddingResponseDto;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Service
@RequiredArgsConstructor
public class BiddingService {

    private final BiddingRepository biddingRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final BiddingPositionRepository positionRepository;
    private final StatusRepository statusRepository;
    private final SizeRepository sizeRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    public Map<String, Object> createBidding(BiddingRequestDto dto, UserEntity user) {
        boolean isMatched = false;
        Long createdOrderId = null;

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

        // 3. 상태
        StatusEntity pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PENDING 상태 없음"));
        StatusEntity matchedStatus = statusRepository.findByName("MATCHED")
                .orElseThrow(() -> new IllegalArgumentException("MATCHED 상태 없음"));

        OrderStatusEntity paymentPendingStatus = orderStatusRepository.findByOrderStatus("PAYMENT_PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PAYMENT_PENDING 주문 상태 없음"));
        // 4. 저장할 BiddingEntity 생성
        BiddingEntity newBidding = new BiddingEntity();
        newBidding.setUser(user);
        newBidding.setProductSize(productSize);
        newBidding.setPosition(position);
        newBidding.setPrice(dto.getPrice());
        newBidding.setCreatedAt(LocalDateTime.now());
        newBidding.setUpdatedAt(LocalDateTime.now());

        // ✅ 즉시 거래 로직
        if (position.getPosition().equals("BUY")) {
            // 구매 입찰 → 가장 낮은 판매 입찰 찾기
            BiddingEntity matchedSell = biddingRepository
                    .findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceAsc(
                            productSize, "SELL", pendingStatus)
                    .filter(sell -> dto.getPrice() >= sell.getPrice())
                    .orElse(null);

            if (matchedSell != null) {
                matchedSell.setStatus(matchedStatus);
                matchedSell.setUpdatedAt(LocalDateTime.now());
                newBidding.setStatus(matchedStatus);
                newBidding = biddingRepository.save(newBidding);
                biddingRepository.save(matchedSell);
                isMatched = true;

                OrderEntity order = new OrderEntity();
                order.setBuyer(user);
                order.setSeller(matchedSell.getUser());
                order.setProductSize(productSize);
                order.setPrice(matchedSell.getPrice());
                order.setCreatedAt(LocalDateTime.now());
                order.setOrderStatus(paymentPendingStatus);
                order.setBidding(newBidding);
                OrderEntity savedOrder = orderRepository.save(order);
                createdOrderId = savedOrder.getId();
            } else {
                newBidding.setStatus(pendingStatus);
                biddingRepository.save(newBidding);
            }

        } else if (position.getPosition().equals("SELL")) {
            // 판매 입찰 → 가장 높은 구매 입찰 찾기
            BiddingEntity matchedBuy = biddingRepository
                    .findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceDesc(
                            productSize, "BUY", pendingStatus)
                    .filter(buy -> dto.getPrice() <= buy.getPrice())
                    .orElse(null);

            if (matchedBuy != null) {
                matchedBuy.setStatus(matchedStatus);
                matchedBuy.setUpdatedAt(LocalDateTime.now());
                newBidding.setStatus(matchedStatus);
                newBidding = biddingRepository.save(newBidding);
                biddingRepository.save(matchedBuy);
                isMatched = true;

                OrderEntity order = new OrderEntity();
                order.setBuyer(matchedBuy.getUser());
                order.setSeller(user); // 판매자 = 현재 사용자
                order.setProductSize(productSize);
                order.setPrice(matchedBuy.getPrice());
                order.setCreatedAt(LocalDateTime.now());
                order.setOrderStatus(paymentPendingStatus);
                order.setBidding(newBidding);
                OrderEntity savedOrder = orderRepository.save(order);
                createdOrderId = savedOrder.getId();
            } else {
                newBidding.setStatus(pendingStatus);
                biddingRepository.save(newBidding);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matched", isMatched);
        result.put("orderId", createdOrderId);
        return result;
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

    public Map<String, Integer> getBiddingSummary(Long productId, String sizeName) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        SizeEntity sizeEntity = sizeRepository.findByName(sizeName)
                .orElseThrow(() -> new IllegalArgumentException("사이즈 없음"));
        ProductSizeEntity productSize = productSizeRepository.findByProductAndSize(product, sizeEntity)
                .orElseThrow(() -> new IllegalArgumentException("해당 사이즈 없음"));

        StatusEntity pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PENDING 상태 없음"));

        // 가장 낮은 판매 입찰가
        Integer lowestAsk = biddingRepository.findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceAsc(
                productSize, "SELL", pendingStatus
        ).map(BiddingEntity::getPrice).orElse(null);

        // 가장 높은 구매 입찰가
        Integer highestBid = biddingRepository.findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceDesc(
                productSize, "BUY", pendingStatus
        ).map(BiddingEntity::getPrice).orElse(null);

        Map<String, Integer> summary = new HashMap<>();
        summary.put("lowestAsk", lowestAsk);
        summary.put("highestBid", highestBid);
        return summary;
    }

}
