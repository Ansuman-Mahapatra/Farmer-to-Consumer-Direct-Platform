package com.server.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;

    @DBRef
    private User farmer;

    private String name;
    private String category;
    private Double pricePerKg;
    private Double availableQuantity;
    private String imageUrl;
    private String description;
    private String language = "en";
}