package com.enterprise.shop.controller;

import com.enterprise.shop.dto.BulkStockUpdateDto;
import com.enterprise.shop.dto.WarehouseStatusDto;
import com.enterprise.shop.service.StockOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for SOAP-proxied operations
 * Exposes REST endpoints that internally call SOAP services
 */
@RestController
@RequestMapping("/v1/warehouse")
@Tag(name = "Warehouse Operations (SOAP)", description = "APIs that proxy SOAP calls to product-stock service")
public class WarehouseController {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseController.class);
    private final StockOrchestrationService stockService;

    public WarehouseController(StockOrchestrationService stockService) {
        this.stockService = stockService;
    }

    // ==========================================================================
    // Use Case 4: SOAP - Bulk stock update
    // ==========================================================================
    @PostMapping(value = "/bulk-update", 
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Bulk stock update", 
               description = "Performs bulk stock update via SOAP service (supports JSON/XML)")
    public ResponseEntity<BulkStockUpdateDto> bulkStockUpdate(
            @Valid @RequestBody BulkStockUpdateDto request) {
        
        logger.info("SOAP PROXY - Bulk stock update for {} items", 
                request.getItems() != null ? request.getItems().size() : 0);
        BulkStockUpdateDto response = stockService.bulkStockUpdate(request);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 5: SOAP - Legacy warehouse status check
    // ==========================================================================
    @GetMapping(value = "/status/{warehouseCode}",
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Get warehouse status", 
               description = "Retrieves warehouse status via SOAP service")
    public ResponseEntity<WarehouseStatusDto> getWarehouseStatus(
            @PathVariable String warehouseCode) {
        
        logger.info("SOAP PROXY - Getting status for warehouse: {}", warehouseCode);
        WarehouseStatusDto response = stockService.getWarehouseStatus(warehouseCode);
        return ResponseEntity.ok(response);
    }
}
