package com.server.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private Role role;
    private String phone;
    private String address;

    @CreatedDate
    private LocalDateTime createdAt;
}

enum Role {
    FARMER, CONSUMER, DELIVERY_PARTNER, ADMIN
}