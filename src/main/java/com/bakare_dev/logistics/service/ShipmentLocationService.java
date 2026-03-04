package com.bakare_dev.logistics.service;

import com.bakare_dev.logistics.dto.request.LocationUpdateRequest;
import com.bakare_dev.logistics.dto.response.ShipmentLocationResponse;
import com.bakare_dev.logistics.dto.response.ShipmentTrackingResponse;

import java.util.List;

public interface ShipmentLocationService {
    ShipmentLocationResponse addLocation(LocationUpdateRequest request);
    List<ShipmentLocationResponse> getLocationHistory(Long shipmentId);
    ShipmentLocationResponse getLatestLocation(Long shipmentId);
    ShipmentTrackingResponse getTrackingInfo(String trackingNumber);
}
