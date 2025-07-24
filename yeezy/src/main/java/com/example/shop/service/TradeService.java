package com.example.shop.service;

import com.example.shop.entity.ProductEntity;
import com.example.shop.entity.StatusEntity;
import com.example.shop.entity.TradeEntity;
import com.example.shop.entity.UserEntity;
import com.example.shop.repository.StatusRepository;
import com.example.shop.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final StatusRepository statusRepository; // COMPLETED 등 조회용

    /**
     * 거래 생성 (즉시 체결 시 호출)
     */
    @Transactional
    public TradeEntity createTrade(
            UserEntity buyer,
            UserEntity seller,
            ProductEntity product,
            String size,
            Integer price
    ) {
        StatusEntity completed = statusRepository.findByName("COMPLETED")
                .orElseThrow(() -> new IllegalArgumentException("COMPLETED 상태가 없습니다."));

        TradeEntity trade = new TradeEntity();
        trade.setBuyer(buyer);
        trade.setSeller(seller);
        trade.setProduct(product);
        trade.setSize(size);
        trade.setPrice(price);
        trade.setStatus(completed);
        trade.setTradedAt(LocalDateTime.now());

        return tradeRepository.save(trade);
    }

    /**
     * 사용자가 참여한 모든 거래(구매+판매) 조회
     */
    @Transactional(readOnly = true)
    public List<TradeEntity> getUserTrades(UserEntity user) {
        return tradeRepository.findByBuyerOrSeller(user, user);
    }
}
