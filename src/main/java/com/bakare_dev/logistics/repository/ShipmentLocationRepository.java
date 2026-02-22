package com.bakare_dev.logistics.repository;

import com.bakare_dev.logistics.entity.Shipment;
import com.bakare_dev.logistics.entity.ShipmentLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ShipmentLocationRepository extends JpaRepository<ShipmentLocation, Long> {
    List<ShipmentLocation> findByShipmentOrderByTimestampDesc(Shipment shipment);
}
