package com.enterprise.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * DTO for stock search response with pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockSearchResponseDto {
    
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
