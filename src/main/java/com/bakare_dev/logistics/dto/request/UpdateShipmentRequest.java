package com.bakare_dev.logistics.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentRequest {
    private String pickupAddress;
    private String deliveryAddress;

    @Positive(message = "Price must be positive")
    private Double price;

    private LocalDateTime estimatedDelivery;
}