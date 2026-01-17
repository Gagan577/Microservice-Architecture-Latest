package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for stock availability check response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockAvailabilityDto {
    private String sku;
    private String productName;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String warehouseCode;
    private String warehouseLocation;
    private Boolean isAvailable;
    private LocalDateTime lastUpdated;
    private String status;
    private String message;
}
