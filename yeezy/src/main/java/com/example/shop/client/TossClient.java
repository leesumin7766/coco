package com.example.shop.client;

import com.example.shop.dto.TossResponseDto;
import com.example.shop.dto.ConfirmPaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossClient {

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public TossResponseDto confirmPayment(ConfirmPaymentRequestDto request) {
        String url = "https://api.tosspayments.com/v1/payments/" + request.getPaymentKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<ConfirmPaymentRequestDto> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<TossResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                TossResponseDto.class
        );

        return response.getBody();
    }
}
