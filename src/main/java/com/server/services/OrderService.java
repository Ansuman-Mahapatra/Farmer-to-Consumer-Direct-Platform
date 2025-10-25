package com.server.services;

import com.server.Models.*;
import com.server.Repositories.OrderRepository;
import com.server.Repositories.ProductRepository;
import com.server.Repositories.UserRepository;
import com.server.controllers.OrderItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RazorpayService razorpayService;

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

    // In your OrderService, update the createOrder method:
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

            // Reduce inventory (temporarily for pending payment)
            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
            productRepository.save(product);

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
            // Create Razorpay order
            String razorpayOrderId = razorpayService.createRazorpayOrder(savedOrder.getId(), totalAmount);
            savedOrder.setRazorpayOrderId(razorpayOrderId);
            return orderRepository.save(savedOrder);

        } catch (Exception e) {
            // If Razorpay fails, restore inventory and throw error
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                product.setAvailableQuantity(product.getAvailableQuantity() + item.getQuantity());
                productRepository.save(product);
            }

            // Delete the order since payment failed
            orderRepository.delete(savedOrder);

            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional
    public void confirmOrderPayment(String orderId, String razorpayPaymentId, String razorpaySignature) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify payment signature
        boolean isValidSignature = razorpayService.verifyPaymentSignature(
                order.getRazorpayOrderId(), razorpayPaymentId, razorpaySignature);

        if (!isValidSignature) {
            throw new RuntimeException("Invalid payment signature");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setRazorpayPaymentId(razorpayPaymentId);
        orderRepository.save(order);

        // Send confirmation emails
        sendOrderConfirmationEmails(order);
    }

    private void sendOrderConfirmationEmails(Order order) {
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