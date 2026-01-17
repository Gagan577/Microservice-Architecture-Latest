package com.enterprise.stock.controller;

import com.enterprise.stock.dto.*;
import com.enterprise.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Stock Operations
 * Product Stock Service - Server B (Port 8081)
 */
@RestController
@RequestMapping("/api/stock")
@Tag(name = "Stock API", description = "Stock management operations")
public class StockRestController {

    private static final Logger logger = LoggerFactory.getLogger(StockRestController.class);
    private final StockService stockService;

    public StockRestController(StockService stockService) {
        this.stockService = stockService;
    }

    // Use Case 1: REST GET - Check item availability
    @GetMapping("/availability/{sku}")
    @Operation(summary = "Check item availability")
    public ResponseEntity<StockAvailabilityDto> checkAvailability(@PathVariable String sku) {
        logger.info("Checking availability for SKU: {}", sku);
        StockAvailabilityDto response = stockService.checkAvailability(sku);
        return ResponseEntity.ok(response);
    }

    // Use Case 2: REST POST - Reserve stock for an order
    @PostMapping("/reservations")
    @Operation(summary = "Reserve stock for order")
    public ResponseEntity<StockReservationDto> reserveStock(@Valid @RequestBody StockReservationDto request) {
        logger.info("Reserving stock for Order: {}", request.getOrderId());
        StockReservationDto response = stockService.reserveStock(request);
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Use Case 3: REST PUT - Update stock threshold
    @PutMapping("/thresholds/{sku}")
    @Operation(summary = "Update stock threshold")
    public ResponseEntity<StockThresholdDto> updateThreshold(
            @PathVariable String sku,
            @Valid @RequestBody StockThresholdDto request) {
        logger.info("Updating threshold for SKU: {}", sku);
        StockThresholdDto response = stockService.updateThreshold(sku, request);
        return ResponseEntity.ok(response);
    }

    // Use Case 8: REST PATCH - Update price adjustments
    @PatchMapping("/products/{sku}/price")
    @Operation(summary = "Adjust product price")
    public ResponseEntity<PriceAdjustmentDto> adjustPrice(
            @PathVariable String sku,
            @Valid @RequestBody PriceAdjustmentDto request) {
        logger.info("Adjusting price for SKU: {}", sku);
        PriceAdjustmentDto response = stockService.adjustPrice(sku, request);
        return ResponseEntity.ok(response);
    }

    // Use Case 9: REST DELETE - Discontinue a product SKU
    @DeleteMapping("/products/{sku}")
    @Operation(summary = "Discontinue product")
    public ResponseEntity<ProductDiscontinueDto> discontinueProduct(
            @PathVariable String sku,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String disposition) {
        logger.info("Discontinuing product SKU: {}", sku);
        ProductDiscontinueDto request = ProductDiscontinueDto.builder()
                .sku(sku)
                .reason(reason)
                .stockDisposition(disposition)
                .build();
        ProductDiscontinueDto response = stockService.discontinueProduct(sku, request);
        return ResponseEntity.ok(response);
    }

    // Use Case 10: REST GET Complex - Search stock with pagination
    @GetMapping("/search")
    @Operation(summary = "Search stock with pagination and filtering")
    public ResponseEntity<StockSearchResponseDto> searchStock(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false, defaultValue = "sku") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        logger.info("Searching stock with filters");
        
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
