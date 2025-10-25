package com.server.services;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    // Add this method to generate valid test signatures
    public String generateTestSignature(String razorpayOrderId, String razorpayPaymentId) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            return calculateRFC2104HMAC(payload, razorpayKeySecret);
        } catch (Exception e) {
            logger.error("Error generating test signature: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate test signature");
        }
    }

    // Method to verify if our signature generation is correct
    public boolean verifySelfGeneratedSignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            String generatedSignature = generateTestSignature(razorpayOrderId, razorpayPaymentId);
            boolean isValid = generatedSignature.equals(signature);

            logger.info("Self-generated signature verification:");
            logger.info("Order ID: {}", razorpayOrderId);
            logger.info("Payment ID: {}", razorpayPaymentId);
            logger.info("Expected Signature: {}", generatedSignature);
            logger.info("Provided Signature: {}", signature);
            logger.info("Verification Result: {}", isValid ? "VALID" : "INVALID");

            return isValid;
        } catch (Exception e) {
            logger.error("Error in self verification: {}", e.getMessage(), e);
            return false;
        }
    }

    public String createRazorpayOrder(String orderId, Double amount) {
        try {
            logger.info("Creating Razorpay order for orderId: {}, amount: {}", orderId, amount);

            if (amount == null || amount <= 0) {
                throw new RuntimeException("Invalid amount: " + amount);
            }

            long amountInPaise = (long) (amount * 100);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId);
            orderRequest.put("payment_capture", 1);

            logger.info("Razorpay order request: {}", orderRequest.toString());

            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            logger.info("Razorpay order created successfully: {}", razorpayOrderId);
            return razorpayOrderId;

        } catch (RazorpayException e) {
            logger.error("Razorpay API error: {}", e.getMessage(), e);
            throw new RuntimeException("Razorpay API error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error creating Razorpay order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = calculateRFC2104HMAC(payload, razorpayKeySecret);
            boolean isValid = generatedSignature.equals(razorpaySignature);

            logger.info("=== RAZORPAY SIGNATURE VERIFICATION ===");
            logger.info("Order ID: {}", razorpayOrderId);
            logger.info("Payment ID: {}", razorpayPaymentId);
            logger.info("Expected Signature: {}", generatedSignature);
            logger.info("Received Signature: {}", razorpaySignature);
            logger.info("Verification Result: {}", isValid ? "✅ VALID" : "❌ INVALID");
            logger.info("=====================================");

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying payment signature: {}", e.getMessage(), e);
            return false;
        }
    }

    private String calculateRFC2104HMAC(String data, String secret) throws Exception {
        String HMAC_SHA256 = "HmacSHA256";
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] hmacData = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hmacData);
    }
}