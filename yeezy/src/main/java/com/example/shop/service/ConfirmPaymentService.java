package com.example.shop.service;

import com.example.shop.client.TossClient;
import com.example.shop.dto.ConfirmPaymentRequestDto;
import com.example.shop.dto.TossResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentService {

    private final TossClient tossClient;
    private final PaymentConfirmationTxService paymentConfirmationTxService;

    public void confirmPayment(ConfirmPaymentRequestDto request) {
        // 외부 PG 호출은 트랜잭션 밖에서 수행한다.
        TossResponseDto tossRes = tossClient.confirmPayment(request);
        paymentConfirmationTxService.applyPaymentConfirmation(request, tossRes);
    }
}
