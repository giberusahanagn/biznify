package com.biznify.inbound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InboundShipmentResponseDTO {
    private Long inboundShipmentId;
    private String skuCode;
    private Double quantity;
    private Long productId;
    private Long warehouseId;
    private Long partnerId;
    private LocalDateTime receivedAt;
}
