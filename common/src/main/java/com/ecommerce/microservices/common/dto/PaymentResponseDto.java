package com.ecommerce.microservices.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Payment API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}