package com.biznify.outbound.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class OutBoundShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long partnerId;        // Who owns the product
    private Long productBatchId;   // Link to product batch
    private Long warehouseId;
    private String shipmentCode;   // Unique code for outbound shipment

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long customerId;
    private String status;         // e.g. CREATED, DISPATCHED, DELIVERED
    private String destination;    // Shipping address or partner location
    private String courier;        // Logistics provider
    private String trackingNumber; // Tracking ID from courier
}
