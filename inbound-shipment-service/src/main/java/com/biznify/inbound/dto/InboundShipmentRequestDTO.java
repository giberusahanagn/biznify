
package com.biznify.inbound.dto;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
public class InboundShipmentRequestDTO {
    private Long partnerId;
    private Long warehouseId;
    private Long deliveryPartnerId;

    private List<InboundBatchDTO> productBatches; // NEW: inbound-specific batch list
}
