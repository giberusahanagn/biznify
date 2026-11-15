package com.biznify.inbound.service;

import com.biznify.inbound.client.ProductBatchClient;
import com.biznify.inbound.dto.InboundShipmentRequestDTO;
import com.biznify.inbound.dto.InboundBatchDTO;
import com.biznify.inbound.dto.ProductBatchRequestDTO;
import com.biznify.inbound.dto.ProductBatchResponseDTO;
import com.biznify.inbound.entity.InboundShipment;
import com.biznify.inbound.repository.InboundShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InboundShipmentServiceImpl implements InboundShipmentService {

    @Autowired
    private InboundShipmentRepository inboundShipmentRepository;

    @Autowired
    private ProductBatchClient productBatchClient;

    @Override
    public void processInboundShipment(InboundShipmentRequestDTO requestDTO) {
        // Save the shipment first
        InboundShipment shipment = new InboundShipment();
        shipment.setPartnerId(requestDTO.getPartnerId());
        shipment.setWarehouseId(requestDTO.getWarehouseId());
        shipment.setDeliveryPartnerId(requestDTO.getDeliveryPartnerId());
        shipment.setShipmentDate(LocalDateTime.now());

        InboundShipment savedShipment = inboundShipmentRepository.save(shipment);

        // Send each batch to product-batch-service
        for (int i = 0; i < requestDTO.getProductBatches().size(); i++) {
            InboundBatchDTO inboundBatch = requestDTO.getProductBatches().get(i);

            // Convert InboundBatchDTO → ProductBatchRequestDTO
            ProductBatchRequestDTO batch = new ProductBatchRequestDTO();
            batch.setSkuCode(inboundBatch.getSkuCode());
            batch.setQuantity(inboundBatch.getQuantity());
            batch.setProductId(inboundBatch.getProductId());
            batch.setStorageCondition(inboundBatch.getStorageCondition());
            batch.setManufacturingDate(inboundBatch.getManufacturingDate());
            batch.setExpiryDate(inboundBatch.getExpiryDate());

            batch.setInboundShipmentId(savedShipment.getId());
            batch.setPartnerId(requestDTO.getPartnerId());
            batch.setWarehouseId(requestDTO.getWarehouseId());
            batch.setDeliveryPartnerId(requestDTO.getDeliveryPartnerId());

            ProductBatchResponseDTO response = productBatchClient.sendBatchToProductService(batch);

            // Save invoice number from the first batch only
            if (i == 0 && response.getInvoiceNumber() != null) {
                savedShipment.setInvoiceNumber(response.getInvoiceNumber());
                inboundShipmentRepository.save(savedShipment); // update after invoice
            }
        }
    }
}
