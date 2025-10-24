package com.server.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "delivery_partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {
    @Id
    private String id;

    @DBRef
    private User user;

    private Double currentLocationLat;
    private Double currentLocationLng;
    private Boolean isAvailable = true;
    private String vehicleType;
}