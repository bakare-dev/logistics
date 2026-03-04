package com.bakare_dev.logistics.service.impl;

import com.bakare_dev.logistics.dto.request.AssignDriverRequest;
import com.bakare_dev.logistics.dto.request.CreateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentRequest;
import com.bakare_dev.logistics.dto.request.UpdateShipmentStatusRequest;
import com.bakare_dev.logistics.dto.response.ShipmentResponse;
import com.bakare_dev.logistics.dto.response.UserResponse;
import com.bakare_dev.logistics.entity.Role;
import com.bakare_dev.logistics.entity.Shipment;
import com.bakare_dev.logistics.entity.ShipmentStatus;
import com.bakare_dev.logistics.entity.User;
import com.bakare_dev.logistics.exception.InvalidOperationException;
import com.bakare_dev.logistics.exception.ResourceNotFoundException;
import com.bakare_dev.logistics.repository.ShipmentRepository;
import com.bakare_dev.logistics.repository.UserRepository;
import com.bakare_dev.logistics.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> VALID_TRANSITIONS = Map.of(
            ShipmentStatus.PENDING, Set.of(ShipmentStatus.ASSIGNED, ShipmentStatus.CANCELLED),
            ShipmentStatus.ASSIGNED, Set.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED),
            ShipmentStatus.IN_TRANSIT, Set.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED),
            ShipmentStatus.DELIVERED, Set.of(),
            ShipmentStatus.CANCELLED, Set.of()
    );

    @Override
    @Transactional
    public ShipmentResponse createShipment(Long clientId, CreateShipmentRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", clientId));

        if (client.getRole() != Role.CLIENT) {
            throw new InvalidOperationException("Only users with CLIENT role can create shipments");
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setClient(client);
        shipment.setPickupAddress(request.getPickupAddress());
        shipment.setDeliveryAddress(request.getDeliveryAddress());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setPrice(request.getPrice());
        shipment.setEstimatedDelivery(request.getEstimatedDelivery());

        shipmentRepository.save(shipment);
        return mapToShipmentResponse(shipment);
    }

    @Override
    public ShipmentResponse getShipmentById(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));
        return mapToShipmentResponse(shipment);
    }

    @Override
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));
        return mapToShipmentResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipment(Long shipmentId, UpdateShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new InvalidOperationException("Can only update shipments in PENDING status");
        }

        if (request.getPickupAddress() != null) {
            shipment.setPickupAddress(request.getPickupAddress());
        }
        if (request.getDeliveryAddress() != null) {
            shipment.setDeliveryAddress(request.getDeliveryAddress());
        }
        if (request.getPrice() != null) {
            shipment.setPrice(request.getPrice());
        }
        if (request.getEstimatedDelivery() != null) {
            shipment.setEstimatedDelivery(request.getEstimatedDelivery());
        }

        shipmentRepository.save(shipment);
        return mapToShipmentResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse assignDriver(Long shipmentId, AssignDriverRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new InvalidOperationException("Can only assign drivers to shipments in PENDING status");
        }

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getDriverId()));

        if (driver.getRole() != Role.DRIVER) {
            throw new InvalidOperationException("Only users with DRIVER role can be assigned as drivers");
        }

        shipment.setDriver(driver);
        shipment.setStatus(ShipmentStatus.ASSIGNED);

        shipmentRepository.save(shipment);
        return mapToShipmentResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        if (!isValidTransition(shipment.getStatus(), request.getStatus())) {
            throw new InvalidOperationException(
                    String.format("Cannot transition from %s to %s", shipment.getStatus(), request.getStatus()));
        }

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDateTime.now());
        }

        shipmentRepository.save(shipment);
        return mapToShipmentResponse(shipment);
    }

    @Override
    @Transactional
    public void cancelShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot cancel a shipment that is already " + shipment.getStatus());
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(shipment);
    }

    @Override
    public List<ShipmentResponse> getShipmentsByClient(Long clientId) {
        return shipmentRepository.findByClientId(clientId).stream()
                .map(this::mapToShipmentResponse)
                .toList();
    }

    @Override
    public List<ShipmentResponse> getShipmentsByDriver(Long driverId) {
        return shipmentRepository.findByDriverId(driverId).stream()
                .map(this::mapToShipmentResponse)
                .toList();
    }

    @Override
    public List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatus(status).stream()
                .map(this::mapToShipmentResponse)
                .toList();
    }

    @Override
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToShipmentResponse)
                .toList();
    }

    private boolean isValidTransition(ShipmentStatus current, ShipmentStatus target) {
        Set<ShipmentStatus> allowed = VALID_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .client(mapToUserResponse(shipment.getClient()))
                .driver(shipment.getDriver() != null ? mapToUserResponse(shipment.getDriver()) : null)
                .pickupAddress(shipment.getPickupAddress())
                .deliveryAddress(shipment.getDeliveryAddress())
                .status(shipment.getStatus())
                .price(shipment.getPrice())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .actualDelivery(shipment.getActualDelivery())
                .createdAt(shipment.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
