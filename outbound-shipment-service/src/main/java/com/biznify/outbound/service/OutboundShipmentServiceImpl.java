package com.biznify.outbound.service;

import com.biznify.outbound.client.ProductBatchClient;
import com.biznify.outbound.dto.OutboundShipmentRequestDTO;
import com.biznify.outbound.dto.OutboundShipmentResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OutboundShipmentServiceImpl implements OutboundShipmentService {

  /*  @Autowired
    private OutboundShipmentRepository outboundShipmentRepository;

    Thread
    @Autowired
    private ProductBatchClient productBatchClient;

    @Override
    public OutboundShipmentResponseDTO processOutboundShipment(OutboundShipmentRequestDTO requestDTO) {
        // Save outbound shipment
        OutboundShipment shipment = new OutboundShipment();
        shipment.setPartnerId(requestDTO.getPartnerId());
        shipment.setCustomerId(requestDTO.getCustomerId()); // OUTBOUND needs customer
        shipment.setShipmentDate(LocalDateTime.now());

        OutboundShipment savedShipment = outboundShipmentRepository.save(shipment);

        // Call product-batch-service to reduce stock
        ProductBatchRequestDTO batchRequest = new ProductBatchRequestDTO();
        batchRequest.setSkuCode(requestDTO.getSkuCode());
        batchRequest.setQuantity(requestDTO.getQuantity());
        batchRequest.setWarehouseId(requestDTO.getWarehouseId());
        batchRequest.setOutboundShipmentId(savedShipment.getId());  // link outbound
        batchRequest.setShipmentType("OUTBOUND");

        ProductBatchResponseDTO batchResponse = productBatchClient.sendBatchToProductService(batchRequest);

        // Save invoice/tracking
        savedShipment.setInvoiceNumber(batchResponse.getInvoiceNumber());
        outboundShipmentRepository.save(savedShipment);

        // Build response
        OutboundShipmentResponseDTO responseDTO = new OutboundShipmentResponseDTO();
        responseDTO.setShipmentId(savedShipment.getId());
        responseDTO.setInvoiceNumber(savedShipment.getInvoiceNumber());
        responseDTO.setQuantityDispatched(requestDTO.getQuantity());
        responseDTO.setSkuCode(requestDTO.getSkuCode());
        responseDTO.setShipmentDate(savedShipment.getShipmentDate());

        return responseDTO;
    }
*/
}
