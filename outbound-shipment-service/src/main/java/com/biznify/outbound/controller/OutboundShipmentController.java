package com.biznify.outbound.controller;

import com.biznify.outbound.dto.OutboundShipmentRequestDTO;
import com.biznify.outbound.dto.OutboundShipmentResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbound")
public class OutboundShipmentController {

    @PostMapping("/create")
    public OutboundShipmentResponseDTO createOutboundShipment(@RequestBody OutboundShipmentRequestDTO request) {
        // Here you can save shipment info to DB, generate shipmentId, etc.
        OutboundShipmentResponseDTO response = new OutboundShipmentResponseDTO();
        response.setOutboundShipmentId(System.currentTimeMillis()); // example ID
      //  response.setStatus("CREATED");
        return response;
    }
}
