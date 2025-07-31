package com.example.shop.controller;

import com.example.shop.dto.ConfirmPaymentRequestDto;
import com.example.shop.security.UserDetailsImpl;
import com.example.shop.service.ConfirmPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class ConfirmPaymentController {

    private final ConfirmPaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(@RequestBody ConfirmPaymentRequestDto request,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        paymentService.confirmPayment(request, userDetails.getUser());
        return ResponseEntity.ok("결제 확인 완료");
    }
}
