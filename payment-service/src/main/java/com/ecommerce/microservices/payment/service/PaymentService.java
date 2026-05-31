package com.ecommerce.microservices.payment.service;

import com.ecommerce.microservices.common.event.order.OrderCreatedEvent;
import com.ecommerce.microservices.common.event.payment.PaymentFailedEvent;
import com.ecommerce.microservices.common.event.payment.PaymentProcessedEvent;
import com.ecommerce.microservices.common.utils.IdGenerator;
import com.ecommerce.microservices.payment.entity.Payment;
import com.ecommerce.microservices.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Service for processing payments
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_PROCESSED_TOPIC = "payment-processed";
    private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

    /**
     * Process payment for order
     * Listens to OrderCreatedEvent
     */
    @KafkaListener(topics = "order-created", groupId = "payment-service")
    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());

        String paymentId = IdGenerator.generatePaymentId();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .transactionReference("TXN-" + System.currentTimeMillis())
                .build();

        paymentRepository.save(payment);

        // Simulate payment processing (70% success rate)
        boolean paymentSuccessful = new Random().nextInt(100) < 70;

        if (paymentSuccessful) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                    .paymentId(paymentId)
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .amount(event.getTotalAmount())
                    .paymentMethod("CREDIT_CARD")
                    .transactionReference(payment.getTransactionReference())
                    .correlationId(event.getCorrelationId())
                    .source("payment-service")
                    .build();

            kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, event.getOrderId(), processedEvent);
            log.info("Payment processed successfully for order: {}", event.getOrderId());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .paymentId(paymentId)
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .amount(event.getTotalAmount())
                    .reason("Payment declined - Insufficient funds")
                    .errorCode("INSUFFICIENT_FUNDS")
                    .correlationId(event.getCorrelationId())
                    .source("payment-service")
                    .build();

            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.getOrderId(), failedEvent);
            log.warn("Payment failed for order: {}", event.getOrderId());
        }
    }
}