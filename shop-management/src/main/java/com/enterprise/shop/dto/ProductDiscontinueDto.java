package com.enterprise.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * DTO for product discontinuation
 * Use Case 9: REST DELETE - Discontinue a product SKU
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDiscontinueDto {
    
    private String sku;
    private String reason;
    private String discontinuedBy;
    private String effectiveDate;
    private Boolean clearRemainingStock;
    private String stockDisposition; // RETURN_TO_SUPPLIER, DISCOUNT_SALE, DONATE, DISPOSE
    private Integer remainingQuantity;
    private Boolean success;
    private String message;
}
