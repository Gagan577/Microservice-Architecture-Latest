package com.enterprise.stock.graphql;

import com.enterprise.stock.dto.*;
import com.enterprise.stock.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL Controller for Product Stock Service
 * Use Cases 6 & 7: Product details query and damaged goods mutation
 */
@Controller
public class StockGraphQLController {

    private static final Logger logger = LoggerFactory.getLogger(StockGraphQLController.class);
    private final StockService stockService;

    public StockGraphQLController(StockService stockService) {
        this.stockService = stockService;
    }

    // ==========================================================================
    // Query: Use Case 6 - Fetch product details + stock count + warehouse location
    // ==========================================================================
    @QueryMapping
    public ProductDetailsDto productDetails(@Argument String sku) {
        logger.info("GraphQL Query: productDetails for SKU: {}", sku);
        return stockService.getProductDetails(sku);
    }

    @QueryMapping
    public StockAvailabilityDto stockAvailability(@Argument String sku) {
        logger.info("GraphQL Query: stockAvailability for SKU: {}", sku);
        return stockService.checkAvailability(sku);
    }

    @QueryMapping
    public WarehouseStatusDto warehouseStatus(@Argument String warehouseCode) {
        logger.info("GraphQL Query: warehouseStatus for: {}", warehouseCode);
        return stockService.getWarehouseStatus(warehouseCode);
    }

    // ==========================================================================
    // Mutation: Use Case 7 - Register damaged goods return
    // ==========================================================================
    @MutationMapping
    public DamagedGoodsReturnDto registerDamagedReturn(@Argument DamagedReturnInput input) {
        logger.info("GraphQL Mutation: registerDamagedReturn for SKU: {}", input.getSku());
        
        DamagedGoodsReturnDto request = DamagedGoodsReturnDto.builder()
                .sku(input.getSku())
                .quantity(input.getQuantity())
                .damageType(input.getDamageType())
                .damageDescription(input.getDamageDescription())
                .warehouseCode(input.getWarehouseCode())
                .reportedBy(input.getReportedBy())
                .notes(input.getNotes())
                .build();
        
        return stockService.registerDamagedReturn(request);
    }

    @MutationMapping
    public StockReservationDto reserveStock(@Argument StockReservationInput input) {
        logger.info("GraphQL Mutation: reserveStock for SKU: {}", input.getSku());
        
        StockReservationDto request = StockReservationDto.builder()
                .sku(input.getSku())
                .orderId(input.getOrderId())
                .quantity(input.getQuantity())
                .warehouseCode(input.getWarehouseCode())
                .customerId(input.getCustomerId())
                .notes(input.getNotes())
                .build();
        
        return stockService.reserveStock(request);
    }

    @MutationMapping
    public StockThresholdDto updateStockThreshold(@Argument String sku, @Argument ThresholdInput input) {
        logger.info("GraphQL Mutation: updateStockThreshold for SKU: {}", sku);
        
        StockThresholdDto request = StockThresholdDto.builder()
                .sku(sku)
                .minThreshold(input.getMinThreshold())
                .maxThreshold(input.getMaxThreshold())
                .reorderPoint(input.getReorderPoint())
                .reorderQuantity(input.getReorderQuantity())
                .warehouseCode(input.getWarehouseCode())
                .autoReorder(input.getAutoReorder())
                .build();
        
        return stockService.updateThreshold(sku, request);
    }

    // Input DTOs for GraphQL
    public record DamagedReturnInput(
            String sku,
            Integer quantity,
            String damageType,
            String damageDescription,
            String warehouseCode,
            String reportedBy,
            String notes
    ) {}

    public record StockReservationInput(
            String sku,
            String orderId,
            Integer quantity,
            String warehouseCode,
            String customerId,
            String notes
    ) {}

    public record ThresholdInput(
            Integer minThreshold,
            Integer maxThreshold,
            Integer reorderPoint,
            Integer reorderQuantity,
            String warehouseCode,
            Boolean autoReorder
    ) {}
}
