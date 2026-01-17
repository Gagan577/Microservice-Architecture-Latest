package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

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
    private String stockDisposition;
    private Integer remainingQuantity;
    private Boolean success;
    private String message;
}
