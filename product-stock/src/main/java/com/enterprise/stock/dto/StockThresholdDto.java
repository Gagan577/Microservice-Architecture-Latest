package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockThresholdDto {
    @NotBlank(message = "SKU is required")
    private String sku;
    @NotNull(message = "Minimum threshold is required")
    @Min(value = 0, message = "Minimum threshold cannot be negative")
    private Integer minThreshold;
    @NotNull(message = "Maximum threshold is required")
    @Min(value = 1, message = "Maximum threshold must be at least 1")
    private Integer maxThreshold;
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;
    @Min(value = 1, message = "Reorder quantity must be at least 1")
    private Integer reorderQuantity;
    private String warehouseCode;
    private Boolean autoReorder;
    private Boolean success;
    private String message;
}
