package com.enterprise.shop.service;

import com.enterprise.shop.client.StockGraphQLClient;
import com.enterprise.shop.client.StockRestClient;
import com.enterprise.shop.dto.*;
import com.enterprise.shop.soap.client.StockSoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stock Orchestration Service
 * Orchestrates all calls to the product-stock service using various protocols
 */
@Service
public class StockOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(StockOrchestrationService.class);

    private final StockRestClient restClient;
    private final StockGraphQLClient graphQLClient;
    private final StockSoapClient soapClient;

    public StockOrchestrationService(StockRestClient restClient,
                                     StockGraphQLClient graphQLClient,
                                     StockSoapClient soapClient) {
        this.restClient = restClient;
        this.graphQLClient = graphQLClient;
        this.soapClient = soapClient;
    }

    // ==========================================================================
    // REST Operations
    // ==========================================================================

    /**
     * Use Case 1: Check item availability (REST GET)
     */
    public StockAvailabilityDto checkAvailability(String sku) {
        logger.info("Orchestrating availability check for SKU: {}", sku);
        try {
            StockAvailabilityDto response = restClient.checkAvailability(sku);
            logger.info("Availability check completed for SKU: {} - Available: {}", 
                    sku, response.getIsAvailable());
            return response;
        } catch (Exception e) {
            logger.error("Availability check failed for SKU: {}", sku, e);
            return StockAvailabilityDto.builder()
                    .sku(sku)
                    .isAvailable(false)
                    .status("ERROR")
                    .message("Failed to check availability: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 2: Reserve stock for an order (REST POST)
     */
    public StockReservationDto reserveStock(StockReservationDto reservation) {
        logger.info("Orchestrating stock reservation for SKU: {}, Order: {}", 
                reservation.getSku(), reservation.getOrderId());
        try {
            StockReservationDto response = restClient.reserveStock(reservation);
            logger.info("Stock reservation completed - Reservation ID: {}", 
                    response.getReservationId());
            return response;
        } catch (Exception e) {
            logger.error("Stock reservation failed for Order: {}", reservation.getOrderId(), e);
            return StockReservationDto.builder()
                    .sku(reservation.getSku())
                    .orderId(reservation.getOrderId())
                    .success(false)
                    .status("FAILED")
                    .message("Reservation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 3: Update stock threshold (REST PUT)
     */
    public StockThresholdDto updateThreshold(String sku, StockThresholdDto threshold) {
        logger.info("Orchestrating threshold update for SKU: {}", sku);
        try {
            threshold.setSku(sku);
            StockThresholdDto response = restClient.updateThreshold(sku, threshold);
            logger.info("Threshold update completed for SKU: {}", sku);
            return response;
        } catch (Exception e) {
            logger.error("Threshold update failed for SKU: {}", sku, e);
            return StockThresholdDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("Threshold update failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 8: Update price adjustments (REST PATCH)
     */
    public PriceAdjustmentDto adjustPrice(String sku, PriceAdjustmentDto adjustment) {
        logger.info("Orchestrating price adjustment for SKU: {}", sku);
        try {
            adjustment.setSku(sku);
            PriceAdjustmentDto response = restClient.adjustPrice(sku, adjustment);
            logger.info("Price adjustment completed for SKU: {} - New Price: {}", 
                    sku, response.getNewPrice());
            return response;
        } catch (Exception e) {
            logger.error("Price adjustment failed for SKU: {}", sku, e);
            return PriceAdjustmentDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("Price adjustment failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 9: Discontinue a product SKU (REST DELETE)
     */
    public ProductDiscontinueDto discontinueProduct(String sku, ProductDiscontinueDto request) {
        logger.info("Orchestrating product discontinuation for SKU: {}", sku);
        try {
            request.setSku(sku);
            ProductDiscontinueDto response = restClient.discontinueProduct(sku, request);
            logger.info("Product discontinuation completed for SKU: {}", sku);
            return response;
        } catch (Exception e) {
            logger.error("Product discontinuation failed for SKU: {}", sku, e);
            return ProductDiscontinueDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("Discontinuation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 10: Search stock with pagination and filtering (REST GET Complex)
     */
    public StockSearchResponseDto searchStock(StockSearchRequestDto request) {
        logger.info("Orchestrating stock search with filters");
        try {
            StockSearchResponseDto response = restClient.searchStock(request);
            logger.info("Stock search completed - Found {} items", 
                    response.getPagination() != null ? response.getPagination().getTotalElements() : 0);
            return response;
        } catch (Exception e) {
            logger.error("Stock search failed", e);
            throw new RuntimeException("Stock search failed: " + e.getMessage(), e);
        }
    }

    // ==========================================================================
    // SOAP Operations
    // ==========================================================================

    /**
     * Use Case 4: Bulk stock update (SOAP)
     */
    public BulkStockUpdateDto bulkStockUpdate(BulkStockUpdateDto request) {
        logger.info("Orchestrating bulk stock update via SOAP - Items: {}", 
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            BulkStockUpdateDto response = soapClient.bulkStockUpdate(request);
            logger.info("Bulk update completed - Success: {}, Failed: {}", 
                    response.getSuccessCount(), response.getFailureCount());
            return response;
        } catch (Exception e) {
            logger.error("Bulk stock update failed", e);
            return BulkStockUpdateDto.builder()
                    .batchId(request.getBatchId())
                    .status("FAILED")
                    .message("Bulk update failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 5: Legacy warehouse status check (SOAP)
     */
    public WarehouseStatusDto getWarehouseStatus(String warehouseCode) {
        logger.info("Orchestrating warehouse status check via SOAP for: {}", warehouseCode);
        try {
            WarehouseStatusDto response = soapClient.getWarehouseStatus(warehouseCode);
            logger.info("Warehouse status retrieved for: {} - Status: {}", 
                    warehouseCode, response.getStatus());
            return response;
        } catch (Exception e) {
            logger.error("Warehouse status check failed for: {}", warehouseCode, e);
            return WarehouseStatusDto.builder()
                    .warehouseCode(warehouseCode)
                    .isOperational(false)
                    .message("Status check failed: " + e.getMessage())
                    .build();
        }
    }

    // ==========================================================================
    // GraphQL Operations
    // ==========================================================================

    /**
     * Use Case 6: Fetch product details (GraphQL Query)
     */
    public ProductDetailsDto fetchProductDetails(String sku) {
        logger.info("Orchestrating product details fetch via GraphQL for SKU: {}", sku);
        try {
            ProductDetailsDto response = graphQLClient.fetchProductDetails(sku);
            logger.info("Product details fetched for SKU: {} - Stock: {}", 
                    sku, response.getStockCount());
            return response;
        } catch (Exception e) {
            logger.error("Product details fetch failed for SKU: {}", sku, e);
            return ProductDetailsDto.builder()
                    .sku(sku)
                    .message("Failed to fetch product details: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Use Case 7: Register damaged goods return (GraphQL Mutation)
     */
    public DamagedGoodsReturnDto registerDamagedReturn(DamagedGoodsReturnDto request) {
        logger.info("Orchestrating damaged goods return via GraphQL for SKU: {}", request.getSku());
        try {
            DamagedGoodsReturnDto response = graphQLClient.registerDamagedReturn(request);
            logger.info("Damaged return registered - Return ID: {}", response.getReturnId());
            return response;
        } catch (Exception e) {
            logger.error("Damaged return registration failed for SKU: {}", request.getSku(), e);
            return DamagedGoodsReturnDto.builder()
                    .sku(request.getSku())
                    .success(false)
                    .status("FAILED")
                    .message("Registration failed: " + e.getMessage())
                    .build();
        }
    }
}
