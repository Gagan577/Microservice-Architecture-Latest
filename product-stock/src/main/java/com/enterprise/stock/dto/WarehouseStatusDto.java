package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseStatusDto {
    private String warehouseCode;
    private String warehouseName;
    private String location;
    private String region;
    private String status;
    private Integer totalCapacity;
    private Integer usedCapacity;
    private Integer availableCapacity;
    private Double utilizationPercentage;
    private Integer totalSkus;
    private Integer lowStockSkus;
    private Integer outOfStockSkus;
    private LocalDateTime lastInventoryCheck;
    private LocalDateTime lastUpdated;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private Boolean isOperational;
    private String message;
}
