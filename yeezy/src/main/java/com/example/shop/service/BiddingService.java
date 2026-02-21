package com.example.shop.service;

import com.example.shop.dto.BiddingRequestDto;
import com.example.shop.dto.BiddingResponseDto;
import com.example.shop.entity.*;
import com.example.shop.observability.service.IdempotencyKeyGenerator;
import com.example.shop.observability.service.ObservabilityService;
import com.example.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObservabilityService observabilityService;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Transactional(timeout = 5)
    public Map<String, Object> createBidding(BiddingRequestDto dto, UserEntity user) {
        boolean isMatched = false;
        Long createdOrderId = null;

        ProductEntity product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        SizeEntity sizeEntity = sizeRepository.findByName(dto.getSize())
                .orElseThrow(() -> new IllegalArgumentException("사이즈 없음"));
        ProductSizeEntity productSize = productSizeRepository.findByProductAndSize(product, sizeEntity)
                .orElseThrow(() -> new IllegalArgumentException("해당 사이즈 없음"));

        BiddingPositionEntity position = positionRepository.findByPosition(dto.getPosition().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 포지션"));

        StatusEntity pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PENDING 상태 없음"));
        StatusEntity matchedStatus = statusRepository.findByName("MATCHED")
                .orElseThrow(() -> new IllegalArgumentException("MATCHED 상태 없음"));

        OrderStatusEntity paymentPendingStatus = orderStatusRepository.findByOrderStatus("PAYMENT_PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PAYMENT_PENDING 주문 상태 없음"));

        BiddingEntity newBidding = new BiddingEntity();
        newBidding.setUser(user);
        newBidding.setProductSize(productSize);
        newBidding.setPosition(position);
        newBidding.setPrice(dto.getPrice());
        newBidding.setCreatedAt(LocalDateTime.now());
        newBidding.setUpdatedAt(LocalDateTime.now());

        if (position.getPosition().equals("BUY")) {
            BiddingEntity matchedSell = biddingRepository
                    .findMatchCandidatesForUpdateAsc(productSize, "SELL", pendingStatus, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
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

                String tradeIdempotencyKey = idempotencyKeyGenerator.forTrade(
                        productSize.getId(),
                        newBidding.getId(),
                        matchedSell.getId()
                );

                observabilityService.saveTradeEvent(
                        savedOrder.getId(),
                        newBidding.getId(),
                        matchedSell.getId(),
                        productSize.getId(),
                        matchedSell.getPrice(),
                        "BIDDING_MATCHED",
                        "MATCHED",
                        "PENDING",
                        "MATCHED",
                        user.getId(),
                        tradeIdempotencyKey,
                        Map.of(
                                "position", "BUY",
                                "requestedPrice", dto.getPrice(),
                                "matchedPrice", matchedSell.getPrice()
                        )
                );

                observabilityService.saveAuditLog(
                        user.getId(),
                        "BIDDING_MATCHED",
                        "BIDDING",
                        String.valueOf(newBidding.getId()),
                        Map.of("status", "PENDING"),
                        Map.of("status", "MATCHED", "orderId", savedOrder.getId()),
                        idempotencyKeyGenerator.forAudit("BIDDING_MATCHED", "BIDDING", String.valueOf(newBidding.getId()))
                );
            } else {
                newBidding.setStatus(pendingStatus);
                BiddingEntity saved = biddingRepository.save(newBidding);
                observabilityService.saveAuditLog(
                        user.getId(),
                        "BIDDING_CREATED",
                        "BIDDING",
                        String.valueOf(saved.getId()),
                        null,
                        Map.of("status", "PENDING", "position", position.getPosition(), "price", saved.getPrice()),
                        idempotencyKeyGenerator.forAudit("BIDDING_CREATED", "BIDDING", String.valueOf(saved.getId()))
                );
            }

        } else if (position.getPosition().equals("SELL")) {
            BiddingEntity matchedBuy = biddingRepository
                    .findMatchCandidatesForUpdateDesc(productSize, "BUY", pendingStatus, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
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
                order.setSeller(user);
                order.setProductSize(productSize);
                order.setPrice(matchedBuy.getPrice());
                order.setCreatedAt(LocalDateTime.now());
                order.setOrderStatus(paymentPendingStatus);
                order.setBidding(newBidding);
                OrderEntity savedOrder = orderRepository.save(order);
                createdOrderId = savedOrder.getId();

                String tradeIdempotencyKey = idempotencyKeyGenerator.forTrade(
                        productSize.getId(),
                        matchedBuy.getId(),
                        newBidding.getId()
                );

                observabilityService.saveTradeEvent(
                        savedOrder.getId(),
                        matchedBuy.getId(),
                        newBidding.getId(),
                        productSize.getId(),
                        matchedBuy.getPrice(),
                        "BIDDING_MATCHED",
                        "MATCHED",
                        "PENDING",
                        "MATCHED",
                        user.getId(),
                        tradeIdempotencyKey,
                        Map.of(
                                "position", "SELL",
                                "requestedPrice", dto.getPrice(),
                                "matchedPrice", matchedBuy.getPrice()
                        )
                );

                observabilityService.saveAuditLog(
                        user.getId(),
                        "BIDDING_MATCHED",
                        "BIDDING",
                        String.valueOf(newBidding.getId()),
                        Map.of("status", "PENDING"),
                        Map.of("status", "MATCHED", "orderId", savedOrder.getId()),
                        idempotencyKeyGenerator.forAudit("BIDDING_MATCHED", "BIDDING", String.valueOf(newBidding.getId()))
                );
            } else {
                newBidding.setStatus(pendingStatus);
                BiddingEntity saved = biddingRepository.save(newBidding);
                observabilityService.saveAuditLog(
                        user.getId(),
                        "BIDDING_CREATED",
                        "BIDDING",
                        String.valueOf(saved.getId()),
                        null,
                        Map.of("status", "PENDING", "position", position.getPosition(), "price", saved.getPrice()),
                        idempotencyKeyGenerator.forAudit("BIDDING_CREATED", "BIDDING", String.valueOf(saved.getId()))
                );
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

    public List<BiddingResponseDto> getSalesByUser(UserEntity user) {
        BiddingPositionEntity sellPosition = positionRepository.findByPosition("SELL")
                .orElseThrow(() -> new IllegalArgumentException("SELL 포지션이 존재하지 않습니다."));

        List<BiddingEntity> sellBiddings = biddingRepository.findByUserAndPosition(user, sellPosition);
        return sellBiddings.stream()
                .map(BiddingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

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

        observabilityService.saveAuditLog(
                user.getId(),
                "BIDDING_CANCELLED",
                "BIDDING",
                String.valueOf(bidding.getId()),
                Map.of("status", "PENDING"),
                Map.of("status", "CANCELLED"),
                idempotencyKeyGenerator.forAudit("BIDDING_CANCELLED", "BIDDING", String.valueOf(bidding.getId()))
        );
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

        Integer lowestAsk = biddingRepository.findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceAsc(
                productSize, "SELL", pendingStatus
        ).map(BiddingEntity::getPrice).orElse(null);

        Integer highestBid = biddingRepository.findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceDesc(
                productSize, "BUY", pendingStatus
        ).map(BiddingEntity::getPrice).orElse(null);

        Map<String, Integer> summary = new HashMap<>();
        summary.put("lowestAsk", lowestAsk);
        summary.put("highestBid", highestBid);
        return summary;
    }
}
