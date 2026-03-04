package com.bakare_dev.logistics.service.impl;

import com.bakare_dev.logistics.dto.request.LocationUpdateRequest;
import com.bakare_dev.logistics.dto.response.ShipmentLocationResponse;
import com.bakare_dev.logistics.dto.response.ShipmentTrackingResponse;
import com.bakare_dev.logistics.entity.Shipment;
import com.bakare_dev.logistics.entity.ShipmentLocation;
import com.bakare_dev.logistics.entity.ShipmentStatus;
import com.bakare_dev.logistics.exception.InvalidOperationException;
import com.bakare_dev.logistics.exception.ResourceNotFoundException;
import com.bakare_dev.logistics.repository.ShipmentLocationRepository;
import com.bakare_dev.logistics.repository.ShipmentRepository;
import com.bakare_dev.logistics.service.ShipmentLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentLocationServiceImpl implements ShipmentLocationService {

    private final ShipmentLocationRepository shipmentLocationRepository;
    private final ShipmentRepository shipmentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCATION_CACHE_PREFIX = "shipment:location:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    @Transactional
    public ShipmentLocationResponse addLocation(LocationUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", request.getShipmentId()));

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidOperationException("Can only update location for shipments in IN_TRANSIT status");
        }

        ShipmentLocation location = new ShipmentLocation();
        location.setShipment(shipment);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setTimestamp(LocalDateTime.now());

        shipmentLocationRepository.save(location);

        ShipmentLocationResponse response = mapToLocationResponse(location);

        redisTemplate.opsForValue().set(
                LOCATION_CACHE_PREFIX + shipment.getId(),
                response,
                CACHE_TTL
        );

        messagingTemplate.convertAndSend(
                "/topic/shipment/" + shipment.getTrackingNumber(),
                response
        );

        return response;
    }

    @Override
    public List<ShipmentLocationResponse> getLocationHistory(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        return shipmentLocationRepository.findByShipmentOrderByTimestampDesc(shipment).stream()
                .map(this::mapToLocationResponse)
                .toList();
    }

    @Override
    public ShipmentLocationResponse getLatestLocation(Long shipmentId) {
        Object cached = redisTemplate.opsForValue().get(LOCATION_CACHE_PREFIX + shipmentId);
        if (cached instanceof ShipmentLocationResponse response) {
            return response;
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        List<ShipmentLocation> locations = shipmentLocationRepository.findByShipmentOrderByTimestampDesc(shipment);
        if (locations.isEmpty()) {
            throw new ResourceNotFoundException("ShipmentLocation", "shipmentId", shipmentId);
        }

        ShipmentLocationResponse response = mapToLocationResponse(locations.getFirst());

        redisTemplate.opsForValue().set(LOCATION_CACHE_PREFIX + shipmentId, response, CACHE_TTL);

        return response;
    }

    @Override
    public ShipmentTrackingResponse getTrackingInfo(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));

        List<ShipmentLocationResponse> locations = shipmentLocationRepository
                .findByShipmentOrderByTimestampDesc(shipment).stream()
                .map(this::mapToLocationResponse)
                .toList();

        return ShipmentTrackingResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .locations(locations)
                .build();
    }

    private ShipmentLocationResponse mapToLocationResponse(ShipmentLocation location) {
        return ShipmentLocationResponse.builder()
                .id(location.getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .timestamp(location.getTimestamp())
                .build();
    }
}
