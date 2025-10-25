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

    // Razorpay fields
    private String razorpayOrderId;
    private String razorpayPaymentId;
}

