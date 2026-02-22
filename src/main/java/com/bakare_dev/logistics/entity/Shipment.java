package com.bakare_dev.logistics.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String trackingNumber;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private String pickupAddress;
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private Double price;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
}
