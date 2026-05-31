package com.ecommerce.microservices.payment.controller;

import com.ecommerce.microservices.common.dto.PaymentResponseDto;
import com.ecommerce.microservices.payment.entity.Payment;
import com.ecommerce.microservices.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Payment Service
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    /**
     * Get payment by payment ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable String paymentId) {
        log.info("Fetching payment: {}", paymentId);
        
        Optional<Payment> payment = paymentRepository.findByPaymentId(paymentId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(mapToDto(payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable String orderId) {
        log.info("Fetching payment for order: {}", orderId);
        
        Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(mapToDto(payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    private PaymentResponseDto mapToDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}