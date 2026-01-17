package com.enterprise.shop.controller;

import com.enterprise.shop.dto.DamagedGoodsReturnDto;
import com.enterprise.shop.dto.ProductDetailsDto;
import com.enterprise.shop.service.StockOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for GraphQL-proxied operations
 * Exposes REST endpoints that internally call GraphQL services
 */
@RestController
@RequestMapping("/v1/products")
@Tag(name = "Product Operations (GraphQL)", description = "APIs that proxy GraphQL calls to product-stock service")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final StockOrchestrationService stockService;

    public ProductController(StockOrchestrationService stockService) {
        this.stockService = stockService;
    }

    // ==========================================================================
    // Use Case 6: GraphQL Query - Fetch product details + stock + warehouse location
    // ==========================================================================
    @GetMapping("/{sku}/details")
    @Operation(summary = "Get product details", 
               description = "Fetches product details with stock and warehouse info via GraphQL")
    public ResponseEntity<ProductDetailsDto> getProductDetails(@PathVariable String sku) {
        
        logger.info("GRAPHQL PROXY - Fetching product details for SKU: {}", sku);
        ProductDetailsDto response = stockService.fetchProductDetails(sku);
        return ResponseEntity.ok(response);
    }

    // ==========================================================================
    // Use Case 7: GraphQL Mutation - Register damaged goods return
    // ==========================================================================
    @PostMapping("/damaged-returns")
    @Operation(summary = "Register damaged return", 
               description = "Registers a damaged goods return via GraphQL mutation")
    public ResponseEntity<DamagedGoodsReturnDto> registerDamagedReturn(
            @Valid @RequestBody DamagedGoodsReturnDto request) {
        
        logger.info("GRAPHQL PROXY - Registering damaged return for SKU: {}", request.getSku());
        DamagedGoodsReturnDto response = stockService.registerDamagedReturn(request);
        return ResponseEntity.ok(response);
    }
}
