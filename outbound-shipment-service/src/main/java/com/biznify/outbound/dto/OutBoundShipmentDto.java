package com.biznify.outbound.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutBoundShipmentDto {

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
