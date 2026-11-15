package com.biznify.productbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundShipmentRequestDTO {
    private Long productId;
    private Long batchId;
    private Long partnerId;
    private Long warehouseId;
    private Double quantity;
    private String skuCode;

    private String shipmentType; // "INBOUND" or "OUTBOUND"
    private Long outboundShipmentId; // optional for outbound

}
