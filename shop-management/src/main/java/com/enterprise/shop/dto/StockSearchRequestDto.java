package com.enterprise.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * DTO for stock search with pagination
 * Use Case 10: REST GET Complex - Search stock with pagination and filtering
 */
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
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private Integer minQuantity;
    private Integer maxQuantity;
    private String sortBy; // sku, productName, quantity, lastUpdated
    private String sortDirection; // ASC, DESC
    private Integer page;
    private Integer size;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class StockSearchResponseDto {
    
    private List<StockSearchItem> items;
    private PaginationInfo pagination;
    private SearchMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSearchItem {
        private String sku;
        private String productName;
        private String category;
        private Integer quantity;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private String warehouseCode;
        private String warehouseLocation;
        private String stockStatus;
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadata {
        private String appliedFilters;
        private String sortBy;
        private String sortDirection;
        private Long searchTimeMs;
    }
}
