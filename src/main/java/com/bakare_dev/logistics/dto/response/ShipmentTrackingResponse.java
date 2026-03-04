package com.bakare_dev.logistics.dto.response;

import com.bakare_dev.logistics.entity.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {
    private String trackingNumber;
    private ShipmentStatus status;
    private List<ShipmentLocationResponse> locations;
}