package com.bakare_dev.logistics.repository;

import com.bakare_dev.logistics.entity.Shipment;
import com.bakare_dev.logistics.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByClientId(Long clientId);

    List<Shipment> findByDriverId(Long driverId);

}
