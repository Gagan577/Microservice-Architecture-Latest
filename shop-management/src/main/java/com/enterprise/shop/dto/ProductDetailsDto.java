package com.enterprise.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product details with stock (GraphQL Query)
 * Use Case 6: GraphQL Query - Fetch product details + stock count + warehouse location
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailsDto {
    
    private String sku;
    private String productName;
    private String description;
    private String category;
    private String brand;
    private BigDecimal unitPrice;
    private String currency;
    private String unitOfMeasure;
    private Double weight;
    private String dimensions;
    
    // Stock information
    private Integer stockCount;
    private Integer reservedCount;
    private Integer availableCount;
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    
    // Warehouse information
    private String warehouseCode;
    private String warehouseName;
    private String warehouseLocation;
    private String warehouseRegion;
    private String aisle;
    private String shelf;
    private String bin;
    
    private LocalDateTime lastStockUpdate;
    private LocalDateTime lastPriceUpdate;
    private Boolean isActive;
    private String message;
}
