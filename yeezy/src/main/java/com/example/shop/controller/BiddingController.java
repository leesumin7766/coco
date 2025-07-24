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
import java.util.Map;

@RestController
@RequestMapping("/api/biddings")
@RequiredArgsConstructor
public class BiddingController {

    private final BiddingService biddingService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBidding(
            @RequestBody BiddingRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> result = biddingService.createBidding(requestDto, userDetails.getUser());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{biddingId}/cancel")
    public ResponseEntity<String> cancelBidding(
            @PathVariable Long biddingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        biddingService.cancelBidding(biddingId, userDetails.getUser());
        return ResponseEntity.ok("입찰이 취소되었습니다.");
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getBiddingSummary(
            @RequestParam Long productId,
            @RequestParam String size
    ) {
        return ResponseEntity.ok(biddingService.getBiddingSummary(productId, size));
    }

}
