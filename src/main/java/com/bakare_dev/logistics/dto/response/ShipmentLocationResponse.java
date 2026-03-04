package com.bakare_dev.logistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentLocationResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}