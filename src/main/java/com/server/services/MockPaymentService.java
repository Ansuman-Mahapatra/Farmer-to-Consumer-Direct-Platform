package com.server.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MockPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentService.class);

    // Store mock payments for verification
    private Map<String, MockPayment> mockPayments = new HashMap<>();

    public String createPaymentOrder(String orderId, Double amount) {
        try {
            logger.info("Creating mock payment order for Order: {}, Amount: ₹{}", orderId, amount);

            String mockOrderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 8);
            String mockPaymentId = "mock_pay_" + UUID.randomUUID().toString().substring(0, 8);

            // Store the mock payment
            MockPayment payment = new MockPayment(mockOrderId, mockPaymentId, orderId, amount, "SUCCESS");
            mockPayments.put(mockOrderId, payment);

            logger.info("✅ Mock payment order created: {}", mockOrderId);
            return mockOrderId;

        } catch (Exception e) {
            logger.error("Error creating mock payment: {}", e.getMessage());
            return "mock_order_error";
        }
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            logger.info("=== MOCK PAYMENT VERIFICATION ===");
            logger.info("Order ID: {}", orderId);
            logger.info("Payment ID: {}", paymentId);
            logger.info("Signature: {}", signature);

            // For mock payments, we accept any paymentId that starts with "mock_pay_"
            boolean isValid = paymentId != null && paymentId.startsWith("mock_pay_");

            logger.info("Payment verification: {}", isValid ? "✅ SUCCESS" : "❌ FAILED");
            logger.info("=================================");

            return isValid;

        } catch (Exception e) {
            logger.error("Error in mock payment verification: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getPaymentDetails(String orderId) {
        MockPayment payment = mockPayments.values().stream()
                .filter(p -> p.getOriginalOrderId().equals(orderId))
                .findFirst()
                .orElse(null);

        if (payment != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", payment.getMockOrderId());
            details.put("paymentId", payment.getMockPaymentId());
            details.put("amount", payment.getAmount());
            details.put("status", payment.getStatus());
            return details;
        }
        return null;
    }

    // Mock payment data class
    private static class MockPayment {
        private String mockOrderId;
        private String mockPaymentId;
        private String originalOrderId;
        private Double amount;
        private String status;

        public MockPayment(String mockOrderId, String mockPaymentId, String originalOrderId, Double amount, String status) {
            this.mockOrderId = mockOrderId;
            this.mockPaymentId = mockPaymentId;
            this.originalOrderId = originalOrderId;
            this.amount = amount;
            this.status = status;
        }

        public String getMockOrderId() { return mockOrderId; }
        public String getMockPaymentId() { return mockPaymentId; }
        public String getOriginalOrderId() { return originalOrderId; }
        public Double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}