package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceAdjustmentDto {
    @NotBlank(message = "SKU is required")
    private String sku;
    private BigDecimal currentPrice;
    @NotNull(message = "New price is required")
    private BigDecimal newPrice;
    private BigDecimal discountPercentage;
    private String adjustmentReason;
    private String adjustedBy;
    private String effectiveFrom;
    private String effectiveUntil;
    private Boolean success;
    private String message;
}
