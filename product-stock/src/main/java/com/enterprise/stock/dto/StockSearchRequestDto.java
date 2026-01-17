package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockSearchRequestDto {
    private String sku;
    private String productName;
    private String category;
    private String warehouseCode;
    private String stockStatus;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
