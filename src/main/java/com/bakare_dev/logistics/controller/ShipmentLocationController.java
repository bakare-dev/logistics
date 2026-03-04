package com.bakare_dev.logistics.controller;

import com.bakare_dev.logistics.annotation.RateLimit;
import com.bakare_dev.logistics.dto.request.LocationUpdateRequest;
import com.bakare_dev.logistics.dto.response.ShipmentLocationResponse;
import com.bakare_dev.logistics.dto.response.ShipmentTrackingResponse;
import com.bakare_dev.logistics.service.ShipmentLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentLocationController {

    private final ShipmentLocationService shipmentLocationService;

    @PostMapping("/locations")
    @PreAuthorize("hasRole('DRIVER')")
    @RateLimit(requests = 120, minutes = 1)
    public ResponseEntity<ShipmentLocationResponse> addLocation(@Valid @RequestBody LocationUpdateRequest request) {
        return new ResponseEntity<>(shipmentLocationService.addLocation(request), HttpStatus.CREATED);
    }

    @GetMapping("/{shipmentId}/locations")
    public ResponseEntity<List<ShipmentLocationResponse>> getLocationHistory(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentLocationService.getLocationHistory(shipmentId));
    }

    @GetMapping("/{shipmentId}/locations/latest")
    public ResponseEntity<ShipmentLocationResponse> getLatestLocation(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentLocationService.getLatestLocation(shipmentId));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentTrackingResponse> getTrackingInfo(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentLocationService.getTrackingInfo(trackingNumber));
    }
}
