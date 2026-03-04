package com.bakare_dev.logistics.service;

import com.bakare_dev.logistics.dto.request.AssignDriverRequest;
import com.bakare_dev.logistics.dto.request.CreateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentStatusRequest;
import com.bakare_dev.logistics.dto.response.ShipmentResponse;
import com.bakare_dev.logistics.entity.ShipmentStatus;

import java.util.List;

public interface ShipmentService {
    ShipmentResponse createShipment(Long clientId, CreateShipmentRequest request);
    ShipmentResponse getShipmentById(Long shipmentId);
    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);
    ShipmentResponse updateShipment(Long shipmentId, UpdateShipmentRequest request);
    ShipmentResponse assignDriver(Long shipmentId, AssignDriverRequest request);
    ShipmentResponse updateStatus(Long shipmentId, UpdateShipmentStatusRequest request);
    void cancelShipment(Long shipmentId);
    List<ShipmentResponse> getShipmentsByClient(Long clientId);
    List<ShipmentResponse> getShipmentsByDriver(Long driverId);
    List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status);
    List<ShipmentResponse> getAllShipments();
}
