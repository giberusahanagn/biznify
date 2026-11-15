//package com.biznify.productbatch.client;
//
//import com.biznify.productbatch.dto.OutboundShipmentRequestDTO;
//import com.biznify.productbatch.dto.OutboundShipmentResponseDTO;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@FeignClient(name = "outbound-shipment-service", fallback = OutboundShipmentClientFallback.class)
//public interface OutboundShipmentClient {
//
//    @PostMapping("/api/outbound-shipments")
//    OutboundShipmentResponseDTO createOutboundShipment(OutboundShipmentRequestDTO request);
//}
