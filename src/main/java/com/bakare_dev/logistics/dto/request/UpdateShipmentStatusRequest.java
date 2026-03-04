package com.bakare_dev.logistics.dto.request;

import com.bakare_dev.logistics.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentStatusRequest {
    @NotNull(message = "Status is required")
    private ShipmentStatus status;
}