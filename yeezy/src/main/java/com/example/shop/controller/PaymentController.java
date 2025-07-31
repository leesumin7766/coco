package com.example.shop.controller;

import com.example.shop.dto.ConfirmPaymentRequestDto;
import com.example.shop.dto.PaymentRequestDto;
import com.example.shop.dto.PaymentResponseDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<PaymentResponseDto> preparePayment(
            @RequestBody PaymentRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PaymentResponseDto response = paymentService.preparePayment(
                request.getOrderId(), request.getAddress(), userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody ConfirmPaymentRequestDto dto) {
        // 결제 확인 로직
        return ResponseEntity.ok().build();
    }
}