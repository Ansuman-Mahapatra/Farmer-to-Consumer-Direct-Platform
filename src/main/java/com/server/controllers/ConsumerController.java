package com.server.controllers;

import com.server.Models.Order;
import com.server.Models.Product;
import com.server.services.OrderService;
import com.server.services.ProductService;
import com.server.services.MockPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumer")
@CrossOrigin(origins = "*")
public class ConsumerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockPaymentService mockPaymentService;

    @GetMapping("/products")
    public ResponseEntity<?> browseProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Product> products = productService.searchProducts(
                    category, minPrice, maxPrice, keyword, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/products/categories")
    public ResponseEntity<?> getCategories() {
        try {
            List<String> categories = productService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getItems(), request.getDeliveryAddress());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order created successfully");
            response.put("order", order);
            response.put("mockOrderId", order.getRazorpayOrderId());
            response.put("paymentInstructions", "Use any payment ID starting with 'mock_pay_' for testing");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/orders/{orderId}/confirm-payment")
    public ResponseEntity<?> confirmPayment(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {

        try {
            String paymentId = request.get("razorpayPaymentId");
            String signature = request.get("razorpaySignature");

            if (paymentId == null || signature == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "razorpayPaymentId and razorpaySignature are required")
                );
            }

            orderService.confirmOrderPayment(orderId, paymentId, signature);

            return ResponseEntity.ok(Map.of("message", "Payment confirmed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders() {
        try {
            List<Order> orders = orderService.getMyOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // Get payment details for an order
    @GetMapping("/orders/{orderId}/payment-details")
    public ResponseEntity<?> getPaymentDetails(@PathVariable String orderId) {
        try {
            Map<String, Object> paymentDetails = mockPaymentService.getPaymentDetails(orderId);
            if (paymentDetails != null) {
                return ResponseEntity.ok(paymentDetails);
            } else {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No payment details found for this order")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}

// Request DTOs (keep these at the bottom of the file)
class PlaceOrderRequest {
    private List<OrderItemRequest> items;
    private String deliveryAddress;

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}

