package com.server.services;

import com.server.Models.*;
import com.server.Repositories.OrderRepository;
import com.server.Repositories.ProductRepository;
import com.server.Repositories.UserRepository;
import com.server.controllers.OrderItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockPaymentService mockPaymentService;

    @Autowired
    private EmailService emailService;

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Order createOrder(List<OrderItemRequest> items, String deliveryAddress) {
        User consumer = getCurrentUser();

        if (!consumer.getRole().toString().equals("CONSUMER")) {
            throw new RuntimeException("Only consumers can place orders");
        }

        Order order = new Order();
        order.setConsumer(consumer);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        // Validate products and calculate total
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            // Check availability
            if (product.getAvailableQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient quantity for product: " + product.getName() +
                        ". Available: " + product.getAvailableQuantity() +
                        ", Requested: " + item.getQuantity());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPricePerKg());

            orderItems.add(orderItem);
            totalAmount += product.getPricePerKg() * item.getQuantity();
        }

        // Validate total amount
        if (totalAmount <= 0) {
            throw new RuntimeException("Order total must be greater than 0");
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        try {
            // Create mock payment order
            String mockOrderId = mockPaymentService.createPaymentOrder(savedOrder.getId(), totalAmount);
            savedOrder.setRazorpayOrderId(mockOrderId);
            return orderRepository.save(savedOrder);

        } catch (Exception e) {
            logger.error("Error creating mock payment: {}", e.getMessage());
            // Continue with order even if payment setup fails
            savedOrder.setRazorpayOrderId("mock_order_fallback");
            return orderRepository.save(savedOrder);
        }
    }

    @Transactional
    public void confirmOrderPayment(String orderId, String paymentId, String signature) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify payment using mock service
        boolean isValidPayment = mockPaymentService.verifyPayment(order.getRazorpayOrderId(), paymentId, signature);

        if (!isValidPayment) {
            throw new RuntimeException("Invalid payment verification");
        }

        // Reduce inventory only after successful payment
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setRazorpayPaymentId(paymentId);
        orderRepository.save(order);

        // Send confirmation emails
        sendOrderConfirmationEmails(order);
    }

    private void sendOrderConfirmationEmails(Order order) {
        try {
            // Send email to consumer
            emailService.sendOrderConfirmationEmail(
                    order.getConsumer().getEmail(),
                    order.getConsumer().getName(),
                    order.getId(),
                    order.getTotalAmount()
            );

            // Send email to farmers for each product
            for (OrderItem item : order.getOrderItems()) {
                User farmer = item.getProduct().getFarmer();
                emailService.sendNewOrderNotificationToFarmer(
                        farmer.getEmail(),
                        farmer.getName(),
                        order.getId(),
                        item.getProduct().getName()
                );
            }

            logger.info("✅ Confirmation emails sent for order: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error sending confirmation emails: {}", e.getMessage());
        }
    }

    public List<Order> getMyOrders() {
        User consumer = getCurrentUser();
        return orderRepository.findByConsumerIdOrderByOrderDateDesc(consumer.getId());
    }

    public Order getOrderById(String orderId) {
        User consumer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify the order belongs to the current consumer
        if (!order.getConsumer().getId().equals(consumer.getId())) {
            throw new RuntimeException("You can only access your own orders");
        }

        return order;
    }
}