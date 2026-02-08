package com.server.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;

    @DBRef
    private User consumer;

    private Double totalAmount;
    private OrderStatus status;

    @DBRef
    private User deliveryPartner;

    private String deliveryAddress;

    @CreatedDate
    private LocalDateTime orderDate;

    // Embedded order items
    private List<OrderItem> orderItems;

    // Mock payment fields (reusing existing field names)
    private String razorpayOrderId;  // Now stores mock order ID
    private String razorpayPaymentId; // Now stores mock payment ID
}

