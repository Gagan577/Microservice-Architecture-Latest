package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer stockCount;
    private Integer reservedCount;
    private Integer availableCount;
    private String stockStatus;
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
