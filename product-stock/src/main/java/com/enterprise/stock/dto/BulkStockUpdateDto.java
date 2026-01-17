package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkStockUpdateDto {
    private String batchId;
    private String warehouseCode;
    private List<StockItemUpdate> items;
    private Integer totalItems;
    private Integer successCount;
    private Integer failureCount;
    private String status;
    private String message;
    private List<UpdateResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItemUpdate {
        private String sku;
        private Integer quantity;
        private String operation;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResult {
        private String sku;
        private Boolean success;
        private String message;
        private Integer previousQuantity;
        private Integer newQuantity;
    }
}
