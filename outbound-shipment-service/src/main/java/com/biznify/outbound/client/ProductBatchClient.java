package com.biznify.outbound.client;

import com.biznify.outbound.dto.OutboundShipmentRequestDTO;
import com.biznify.outbound.dto.OutboundShipmentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "outbound-shipment-service", url = "http://localhost:8084/api/outbound")
public interface ProductBatchClient {

    @PostMapping("/create")
    OutboundShipmentResponseDTO createOutboundShipment(OutboundShipmentRequestDTO request);
}
