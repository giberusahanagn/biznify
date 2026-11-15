package com.biznify.outbound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OutboundShipmentResponseDTO {
    private Long outboundShipmentId;
    private String skuCode;
    private Double quantity;
    private Long productId;
    private Long warehouseId;
    private Long partnerId;
    private Long customerId;
    private LocalDateTime dispatchedAt;
}
