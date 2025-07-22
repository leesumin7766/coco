package com.example.shop.controller;

import com.example.shop.dto.BiddingRequestDto;
import com.example.shop.dto.BiddingResponseDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.BiddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage/biddings")
@RequiredArgsConstructor
public class BiddingController {

    private final BiddingService biddingService;

    @GetMapping("/buys")
    public List<BiddingResponseDto> getMyBuys(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return biddingService.getBuysByUser(userDetails.getUser());
    }

    @GetMapping("/sales")
    public List<BiddingResponseDto> getMySales(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return biddingService.getSalesByUser(userDetails.getUser());
    }
    @PostMapping
    public ResponseEntity<String> createBidding(
            @RequestBody BiddingRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        biddingService.createBidding(requestDto, userDetails.getUser());
        return ResponseEntity.ok("입찰 등록 성공");
    }

    @PutMapping("/{biddingId}/cancel")
    public ResponseEntity<String> cancelBidding(
            @PathVariable Long biddingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        biddingService.cancelBidding(biddingId, userDetails.getUser());
        return ResponseEntity.ok("입찰이 취소되었습니다.");
    }

}
