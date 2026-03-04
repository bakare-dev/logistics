package com.bakare_dev.logistics.dto.response;

import com.bakare_dev.logistics.entity.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String trackingNumber;
    private UserResponse client;
    private UserResponse driver;
    private String pickupAddress;
    private String deliveryAddress;
    private ShipmentStatus status;
    private Double price;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt;
}