package com.server.controllers;

import com.server.services.RazorpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class SignatureTestController {

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/generate-test-signature")
    public ResponseEntity<?> generateTestSignature(
            @RequestBody SignatureTestRequest request) {

        try {
            String signature = razorpayService.generateTestSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", request.getRazorpayOrderId());
            response.put("razorpayPaymentId", request.getRazorpayPaymentId());
            response.put("generatedSignature", signature);
            response.put("verificationPayload", request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId());
            response.put("secretKeyUsed", "Your Razorpay Secret (masked)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/verify-test-signature")
    public ResponseEntity<?> verifyTestSignature(
            @RequestBody SignatureVerificationRequest request) {

        try {
            boolean isValid = razorpayService.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", request.getRazorpayOrderId());
            response.put("razorpayPaymentId", request.getRazorpayPaymentId());
            response.put("providedSignature", request.getRazorpaySignature());
            response.put("isValid", isValid);

            // Also test self-generation
            String expectedSignature = razorpayService.generateTestSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId()
            );
            response.put("expectedSignature", expectedSignature);
            response.put("signatureMatches", expectedSignature.equals(request.getRazorpaySignature()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/complete-payment-test")
    public ResponseEntity<?> completePaymentTest(
            @RequestBody CompletePaymentTestRequest request) {

        try {
            // Step 1: Generate a valid signature
            String validSignature = razorpayService.generateTestSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId()
            );

            // Step 2: Verify the signature
            boolean isValid = razorpayService.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    validSignature
            );

            Map<String, Object> response = new HashMap<>();
            response.put("testScenario", "Complete Payment Flow Test");
            response.put("razorpayOrderId", request.getRazorpayOrderId());
            response.put("razorpayPaymentId", request.getRazorpayPaymentId());
            response.put("generatedValidSignature", validSignature);
            response.put("verificationResult", isValid ? "SUCCESS" : "FAILED");
            response.put("message", isValid ?
                    "✅ Signature verification working correctly!" :
                    "❌ Signature verification failed!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}

// Request DTOs for testing
class SignatureTestRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
}

class SignatureVerificationRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
}

class CompletePaymentTestRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
}