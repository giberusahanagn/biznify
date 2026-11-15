package com.biznify.outbound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OutboundShipmentRequestDTO {
    private String skuCode;
    private double quantity;
    private Long productId;
    private Long partnerId;
    private Long warehouseId;
    private Long customerId; // ✅ outbound-specific
}
