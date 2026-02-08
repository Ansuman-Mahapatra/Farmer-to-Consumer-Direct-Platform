package com.server.controllers;

import com.server.services.MockPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class MockPaymentTestController {

    @Autowired
    private MockPaymentService mockPaymentService;

    @GetMapping("/mock-payment-connection")
    public ResponseEntity<?> testMockPaymentConnection() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Mock payment service is working correctly");
            response.put("paymentType", "MOCK_PAYMENT");
            response.put("instructions", "Use payment IDs starting with 'mock_pay_' for testing");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/generate-test-payment")
    public ResponseEntity<?> generateTestPayment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            Double amount = Double.parseDouble(request.getOrDefault("amount", "100.0"));

            String mockOrderId = mockPaymentService.createPaymentOrder(orderId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("amount", amount);
            response.put("mockOrderId", mockOrderId);
            response.put("testPaymentId", "mock_pay_" + UUID.randomUUID().toString().substring(0, 8));
            response.put("testSignature", "mock_sig_" + UUID.randomUUID().toString().substring(0, 8));
            response.put("instructions", "Use these test values to confirm payment");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/verify-test-payment")
    public ResponseEntity<?> verifyTestPayment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String paymentId = request.get("paymentId");
            String signature = request.get("signature");

            boolean isValid = mockPaymentService.verifyPayment(orderId, paymentId, signature);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("paymentId", paymentId);
            response.put("signature", signature);
            response.put("isValid", isValid);
            response.put("message", isValid ? "✅ Payment verification successful" : "❌ Payment verification failed");
            response.put("note", "Mock payments accept any paymentId starting with 'mock_pay_'");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/complete-payment-test")
    public ResponseEntity<?> completePaymentTest(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.getOrDefault("orderId", "test_order_" + UUID.randomUUID().toString().substring(0, 8));
            Double amount = Double.parseDouble(request.getOrDefault("amount", "150.0"));

            // Step 1: Create mock payment order
            String mockOrderId = mockPaymentService.createPaymentOrder(orderId, amount);

            // Step 2: Generate test payment data
            String testPaymentId = "mock_pay_" + UUID.randomUUID().toString().substring(0, 8);
            String testSignature = "mock_sig_" + UUID.randomUUID().toString().substring(0, 8);

            // Step 3: Verify payment
            boolean isValid = mockPaymentService.verifyPayment(mockOrderId, testPaymentId, testSignature);

            Map<String, Object> response = new HashMap<>();
            response.put("testScenario", "Complete Mock Payment Flow Test");
            response.put("orderId", orderId);
            response.put("amount", amount);
            response.put("mockOrderId", mockOrderId);
            response.put("testPaymentId", testPaymentId);
            response.put("testSignature", testSignature);
            response.put("verificationResult", isValid ? "SUCCESS" : "FAILED");
            response.put("message", isValid ?
                    "✅ Mock payment flow working correctly!" :
                    "❌ Mock payment flow failed!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/payment-instructions")
    public ResponseEntity<?> getPaymentInstructions() {
        Map<String, Object> instructions = new HashMap<>();
        instructions.put("paymentSystem", "MOCK PAYMENT SYSTEM");
        instructions.put("description", "This is a mock payment system for testing. No real payments are processed.");
        instructions.put("howToTest", "Follow these steps:");
        instructions.put("steps", new String[]{
                "1. Place an order via /api/consumer/orders",
                "2. Get the mockOrderId from the response",
                "3. Confirm payment using any paymentId starting with 'mock_pay_'",
                "4. Use any signature value",
                "5. Order status will change to CONFIRMED"
        });
        instructions.put("validPaymentIdExamples", new String[]{
                "mock_pay_123456",
                "mock_pay_abc123",
                "mock_pay_test001"
        });
        instructions.put("validSignatureExamples", new String[]{
                "any_signature_here",
                "test_sig_123",
                "mock_signature"
        });

        return ResponseEntity.ok(instructions);
    }
}