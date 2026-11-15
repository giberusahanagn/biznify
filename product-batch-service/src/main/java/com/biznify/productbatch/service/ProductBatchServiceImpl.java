package com.biznify.productbatch.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.biznify.productbatch.dto.*;
import com.biznify.productbatch.entity.ProductBatch;
import com.biznify.productbatch.enums.ProductBatchStatus;
import com.biznify.productbatch.exceptions.BinAllocationException;
import com.biznify.productbatch.exceptions.ResourceNotFoundException;
import com.biznify.productbatch.feign.OutboundShipmentClient;
import com.biznify.productbatch.jmsMessagePublisher.NewProductBatchEventPublisher;
import com.biznify.productbatch.repository.ProductBatchRepository;
import com.biznify.shared_events.dto.NewProductBatchInfoToPartner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class ProductBatchServiceImpl implements ProductBatchService {

    @Autowired
    private ProductBatchRepository productBatchRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NewProductBatchEventPublisher eventPublisher;

    @Autowired
    private OutboundShipmentClient outboundShipmentClient;

    private static final String PRODUCT_SERVICE_BASE_URL = "http://localhost:8082/api/products/sku/";
    private static final String PARTNER_SERVICE_BASE_URL = "http://localhost:8081/api/partners/";
    private static final String WAREHOUSE_SERVICE_BASE_URL = "http://localhost:8083/api/warehouses";
    private static final String BIN_SERVICE_BASE_URL = "http://localhost:8083/api/bins";

    // ───────── CREATE BATCH ─────────
    @Override
    public ProductBatchResponseDTO createBatch(ProductBatchRequestDTO dto) {
        validateProductPartnerWarehouse(dto);

        if (productBatchRepository.existsByBatchCode(dto.getBatchCode())) {
            throw new IllegalArgumentException("Batch code already exists");
        }
        if (productBatchRepository.existsBySkuCode(dto.getSkuCode())) {
            throw new IllegalArgumentException("SKU code already exists");
        }

        ProductDTO product = fetchProductBySku(dto.getSkuCode());

        ProductBatch batch = new ProductBatch();
        BeanUtils.copyProperties(dto, batch);
        batch.setProductId(product.getId());
        batch.setPartnerId(product.getPartnerId());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        batch.setStatus(ProductBatchStatus.ACTIVE);

        return convertToDTO(productBatchRepository.save(batch));
    }

    // ───────── STORE PRODUCT BATCH ─────────
    @Override
    public ProductBatchResponseDTO storeProductBatch(ProductBatchRequestDTO dto) {
        validateProductPartnerWarehouse(dto);
        ProductDTO product = fetchProductBySku(dto.getSkuCode());

        if ("OUTBOUND".equalsIgnoreCase(dto.getShipmentType())) {
            return handleOutboundBatch(dto, product);
        } else {
            return handleInboundBatch(dto, product);
        }
    }

    // ───────── INBOUND HANDLER ─────────
    private ProductBatchResponseDTO handleInboundBatch(ProductBatchRequestDTO dto, ProductDTO product) {
        double totalVolume = safeDouble(dto.getQuantity()) *
                safeDouble(product.getLengthCm()) *
                safeDouble(product.getWidthCm()) *
                safeDouble(product.getHeightCm());
        log.info("Total volume to store: {}", totalVolume);

        BinAllocationRequestDTO binRequest = new BinAllocationRequestDTO();
        binRequest.setLengthCm(product.getLengthCm());
        binRequest.setWidthCm(product.getWidthCm());
        binRequest.setHeightCm(product.getHeightCm());
        binRequest.setUnitsToStore(dto.getQuantity());
        binRequest.setWarehouseId(dto.getWarehouseId());
        binRequest.setSkuCode(dto.getSkuCode());

        List<BinLocationResponseDTO> bins = allocateBins(binRequest);
        if (bins == null || bins.isEmpty()) {
            throw new BinAllocationException("No bins allocated for the product batch", null);
        }

        double remainingUnits = dto.getQuantity();
        List<ProductBatch> batchesToSave = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long baseSequence = productBatchRepository.count();
        ProductBatch tempBatch = null;

        for (BinLocationResponseDTO bin : bins) {
            if (remainingUnits <= 0) break;

            double unitsToStore = Math.min(remainingUnits, bin.getUnitsAllocated());
            ProductBatch batch = new ProductBatch();
            BeanUtils.copyProperties(dto, batch);
            batch.setStatus(ProductBatchStatus.STORED);
            batch.setCreatedAt(LocalDateTime.now());
            batch.setUpdatedAt(LocalDateTime.now());
            batch.setBinCode(bin.getBinCode());
            batch.setBinId(bin.getBinId());
            batch.setWarehouseId(dto.getWarehouseId());
            batch.setQuantity(unitsToStore);

            long sequence = baseSequence + batchesToSave.size() + 1;
            batch.setBatchCode("BC-" + today + "-" + sequence);
            batch.setInvoiceNumber("INV-" + today + "-" + sequence);

            batch.setLengthCm(product.getLengthCm());
            batch.setWidthCm(product.getWidthCm());
            batch.setHeightCm(product.getHeightCm());
            batch.setProductId(product.getId());
            batch.setPartnerId(product.getPartnerId());

            batchesToSave.add(batch);
            remainingUnits -= unitsToStore;
            tempBatch = batch;
        }

        if (remainingUnits > 0) {
            throw new BinAllocationException("Unable to allocate all units. Units left: " + remainingUnits, null);
        }

        List<ProductBatch> savedBatches = productBatchRepository.saveAll(batchesToSave);

        PartnerDTO partner = fetchPartnerFromService(dto.getPartnerId());

        savedBatches.forEach(batch -> {
            NewProductBatchInfoToPartner event = new NewProductBatchInfoToPartner();
            event.setBatchId(batch.getId());
            event.setBatchCode(batch.getBatchCode());
            event.setInvoiceNumber(batch.getInvoiceNumber());
            event.setQuantity(batch.getQuantity());
            event.setSkuCode(batch.getSkuCode());
            event.setPartnerId(batch.getPartnerId());
            event.setWarehouseId(batch.getWarehouseId());
            event.setTimestamp(LocalDateTime.now());
            try {
                eventPublisher.sendBatchEvent(event);
            } catch (Exception e) {
                log.error("Failed to send JMS event", e);
            }
        });

        return convertToDTO(savedBatches.get(0));
    }

    // ───────── OUTBOUND HANDLER ─────────
    private ProductBatchResponseDTO handleOutboundBatch(ProductBatchRequestDTO dto, ProductDTO product) {
        List<ProductBatch> batches = productBatchRepository.getByWarehouseId(dto.getWarehouseId()).stream()
                .filter(b -> b.getSkuCode().equals(dto.getSkuCode()) && b.getQuantity() >= dto.getQuantity())
                .toList();

        if (batches.isEmpty()) {
            throw new IllegalArgumentException("Not enough quantity available in warehouse for SKU: " + dto.getSkuCode());
        }

        ProductBatch batch = batches.get(0);
        batch.setQuantity(batch.getQuantity() - dto.getQuantity());
        batch.setUpdatedAt(LocalDateTime.now());
        productBatchRepository.save(batch);

        OutboundShipmentRequestDTO shipmentRequest = OutboundShipmentRequestDTO.builder()
                .batchId(batch.getId())
                .productId(batch.getProductId())
                .partnerId(batch.getPartnerId())
                .warehouseId(batch.getWarehouseId())
                .quantity(dto.getQuantity())
                .skuCode(batch.getSkuCode())
                .build();

        OutboundShipmentResponseDTO shipmentResponse = outboundShipmentClient.createOutboundShipment(shipmentRequest);
        batch.setOutboundShipmentId(shipmentResponse.getShipmentId());
        productBatchRepository.save(batch);

        return convertToDTO(batch);
    }

    // ───────── COMMON METHODS ─────────
    @Override
    public List<ProductBatchResponseDTO> getAll() {
        return productBatchRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductBatchResponseDTO getById(Long id) {
        ProductBatch batch = productBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
        return convertToDTO(batch);
    }

    @Override
    public ProductBatchResponseDTO updateStatus(Long id, String status) {
        ProductBatch batch = productBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
        batch.setStatus(ProductBatchStatus.valueOf(status.toUpperCase()));
        batch.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(productBatchRepository.save(batch));
    }

    @Override
    public ProductBatch updateBatchQuantity(Long batchId, Double quantity) {
        ProductBatch batch = productBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));
        batch.setQuantity(batch.getQuantity() + quantity);
        batch.setUpdatedAt(LocalDateTime.now());
        return productBatchRepository.save(batch);
    }

    @Override
    public List<ProductBatch> getBatchesByWarehouse(Long warehouseId) {
        return productBatchRepository.getByWarehouseId(warehouseId);
    }

    // ───────── VALIDATION & EXTERNAL CALLS ─────────
    private void validateProductPartnerWarehouse(ProductBatchRequestDTO dto) {
        if (dto.getWarehouseId() == null) {
            throw new IllegalArgumentException("Warehouse ID is required");
        }
        validateWarehouse(dto.getWarehouseId());

        ProductDTO product = fetchProductBySku(dto.getSkuCode());
        validatePartner(product.getPartnerId());
    }

    private ProductDTO fetchProductBySku(String skuCode) {
        try {
            return webClientBuilder.baseUrl(PRODUCT_SERVICE_BASE_URL).build()
                    .get().uri("/{sku}", skuCode)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> Mono.error(new ResourceNotFoundException("Product not found with SKU: " + skuCode)))
                    .bodyToMono(ProductDTO.class).block();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error fetching product: " + e.getMessage());
        }
    }

    private void validatePartner(Long partnerId) {
        try {
            webClientBuilder.baseUrl(PARTNER_SERVICE_BASE_URL).build()
                    .get().uri("/{id}", partnerId)
                    .retrieve().toBodilessEntity().block();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to validate partner ID: " + e.getMessage());
        }
    }

    private void validateWarehouse(Long warehouseId) {
        try {
            webClientBuilder.baseUrl(WAREHOUSE_SERVICE_BASE_URL).build()
                    .get().uri("/{id}", warehouseId)
                    .retrieve().toBodilessEntity().block();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to validate warehouse ID: " + e.getMessage());
        }
    }

    private List<BinLocationResponseDTO> allocateBins(BinAllocationRequestDTO request) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(BIN_SERVICE_BASE_URL + "/allocate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BinLocationResponseDTO>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                    .block();
        } catch (Exception ex) {
            throw new BinAllocationException("Failed to allocate bins", ex);
        }
    }

    private ProductBatchResponseDTO convertToDTO(ProductBatch batch) {
        ProductBatchResponseDTO dto = new ProductBatchResponseDTO();
        BeanUtils.copyProperties(batch, dto);
        dto.setStatus(batch.getStatus());
        return dto;
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private PartnerDTO fetchPartnerFromService(Long partnerId) {
        try {
            return webClientBuilder.baseUrl(PARTNER_SERVICE_BASE_URL).build()
                    .get().uri("/{id}", partnerId)
                    .retrieve()
                    .bodyToMono(PartnerDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch partner info", e);
        }
    }
}
