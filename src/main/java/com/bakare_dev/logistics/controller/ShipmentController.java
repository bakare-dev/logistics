package com.bakare_dev.logistics.controller;

import com.bakare_dev.logistics.dto.request.AssignDriverRequest;
import com.bakare_dev.logistics.dto.request.CreateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentStatusRequest;
import com.bakare_dev.logistics.dto.response.ShipmentResponse;
import com.bakare_dev.logistics.entity.ShipmentStatus;
import com.bakare_dev.logistics.security.CustomUserDetails;
import com.bakare_dev.logistics.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ShipmentResponse> createShipment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @Valid @RequestBody CreateShipmentRequest request) {
        return new ResponseEntity<>(shipmentService.createShipment(userDetails.getUser().getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, request));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ShipmentResponse> assignDriver(@PathVariable Long id,
                                                         @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.ok(shipmentService.assignDriver(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<ShipmentResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Void> cancelShipment(@PathVariable Long id) {
        shipmentService.cancelShipment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByClient(clientId));
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByDriver(driverId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByStatus(@PathVariable ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.getShipmentsByStatus(status));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ShipmentResponse>> getMyShipments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(shipmentService.getShipmentsByClient(userDetails.getUser().getId()));
    }

    @GetMapping("/my/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<ShipmentResponse>> getMyDriverShipments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(shipmentService.getShipmentsByDriver(userDetails.getUser().getId()));
    }
}
