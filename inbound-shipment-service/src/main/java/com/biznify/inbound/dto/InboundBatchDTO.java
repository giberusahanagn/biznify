package com.biznify.inbound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InboundBatchDTO {
    private String skuCode;
    private Double quantity;
    private Long productId;
    private String storageCondition;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
}
