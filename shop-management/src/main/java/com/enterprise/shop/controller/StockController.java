package com.enterprise.shop.controller;

import com.enterprise.shop.dto.*;
import com.enterprise.shop.service.StockOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Stock Operations
 * Exposes endpoints that orchestrate calls to product-stock service
 */
@RestController
@RequestMapping("/v1/stock")
@Tag(name = "Stock Operations", description = "APIs for managing stock via product-stock service")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    private final StockOrchestrationService stockService;

    public StockController(StockOrchestrationService stockService) {
        this.stockService = stockService;
    }

    // ==========================================================================
    // Use Case 1: REST GET - Check item availability
    // ==========================================================================
    @GetMapping("/availability/{sku}")
    @Operation(summary = "Check item availability", 
               description = "Checks stock availability for a specific SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability check successful",
                    content = @Content(schema = @Schema(implementation = StockAvailabilityDto.class))),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StockAvailabilityDto> checkAvailability(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku) {
        
        logger.info("REST GET - Checking availability for SKU: {}", sku);
        StockAvailabilityDto response = stockService.checkAvailability(sku);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 2: REST POST - Reserve stock for an order
    // ==========================================================================
    @PostMapping("/reservations")
    @Operation(summary = "Reserve stock", 
               description = "Creates a stock reservation for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(schema = @Schema(implementation = StockReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StockReservationDto> reserveStock(
            @Valid @RequestBody StockReservationDto reservation) {
        
        logger.info("REST POST - Reserving stock for Order: {}", reservation.getOrderId());
        StockReservationDto response = stockService.reserveStock(reservation);
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ==========================================================================
    // Use Case 3: REST PUT - Update stock threshold
    // ==========================================================================
    @PutMapping("/thresholds/{sku}")
    @Operation(summary = "Update stock threshold", 
               description = "Updates min/max stock thresholds for a SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threshold updated successfully",
                    content = @Content(schema = @Schema(implementation = StockThresholdDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StockThresholdDto> updateThreshold(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku,
            @Valid @RequestBody StockThresholdDto threshold) {
        
        logger.info("REST PUT - Updating threshold for SKU: {}", sku);
        StockThresholdDto response = stockService.updateThreshold(sku, threshold);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 8: REST PATCH - Update price adjustments
    // ==========================================================================
    @PatchMapping("/products/{sku}/price")
    @Operation(summary = "Adjust product price", 
               description = "Applies price adjustment to a product SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price adjusted successfully",
                    content = @Content(schema = @Schema(implementation = PriceAdjustmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PriceAdjustmentDto> adjustPrice(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku,
            @Valid @RequestBody PriceAdjustmentDto adjustment) {
        
        logger.info("REST PATCH - Adjusting price for SKU: {}", sku);
        PriceAdjustmentDto response = stockService.adjustPrice(sku, adjustment);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 9: REST DELETE - Discontinue a product SKU
    // ==========================================================================
    @DeleteMapping("/products/{sku}")
    @Operation(summary = "Discontinue product", 
               description = "Marks a product SKU as discontinued")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product discontinued successfully",
                    content = @Content(schema = @Schema(implementation = ProductDiscontinueDto.class))),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProductDiscontinueDto> discontinueProduct(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String disposition) {
        
        logger.info("REST DELETE - Discontinuing product SKU: {}", sku);
        ProductDiscontinueDto request = ProductDiscontinueDto.builder()
                .sku(sku)
                .reason(reason)
                .stockDisposition(disposition)
                .build();
        ProductDiscontinueDto response = stockService.discontinueProduct(sku, request);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 10: REST GET Complex - Search stock with pagination and filtering
    // ==========================================================================
    @GetMapping("/search")
    @Operation(summary = "Search stock", 
               description = "Search stock with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = StockSearchResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StockSearchResponseDto> searchStock(
            @Parameter(description = "Filter by SKU (partial match)")
            @RequestParam(required = false) String sku,
            @Parameter(description = "Filter by product name (partial match)")
            @RequestParam(required = false) String productName,
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter by warehouse code")
            @RequestParam(required = false) String warehouseCode,
            @Parameter(description = "Filter by stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)")
            @RequestParam(required = false) String stockStatus,
            @Parameter(description = "Minimum quantity filter")
            @RequestParam(required = false) Integer minQuantity,
            @Parameter(description = "Maximum quantity filter")
            @RequestParam(required = false) Integer maxQuantity,
            @Parameter(description = "Sort by field (sku, productName, quantity, lastUpdated)")
            @RequestParam(required = false, defaultValue = "sku") String sortBy,
            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        logger.info("REST GET - Searching stock with filters");
        
        StockSearchRequestDto request = StockSearchRequestDto.builder()
                .sku(sku)
                .productName(productName)
                .category(category)
                .warehouseCode(warehouseCode)
                .stockStatus(stockStatus)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();
        
        StockSearchResponseDto response = stockService.searchStock(request);
        return ResponseEntity.ok(response);
    }
}
