/*
package com.example.shop.controller;

import com.example.shop.dto.BiddingRequestDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.BiddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/biddings")
@RequiredArgsConstructor
public class BiddingUpdateController {

    private final BiddingService biddingService;

    @PostMapping
    public ResponseEntity<String> createBidding(
            @RequestBody BiddingRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        biddingService.createBidding(requestDto, userDetails.getUser());
        return ResponseEntity.ok("입찰 등록 성공");
    }
}
*/
