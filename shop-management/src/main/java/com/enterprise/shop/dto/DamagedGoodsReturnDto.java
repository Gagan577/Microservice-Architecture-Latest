package com.enterprise.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for damaged goods return (GraphQL Mutation)
 * Use Case 7: GraphQL Mutation - Register damaged goods return
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamagedGoodsReturnDto {
    
    private String returnId;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotBlank(message = "Damage type is required")
    private String damageType; // PHYSICAL, WATER, EXPIRED, DEFECTIVE, OTHER
    
    @NotBlank(message = "Damage description is required")
    private String damageDescription;
    
    private String warehouseCode;
    private String reportedBy;
    private String inspectedBy;
    private String disposition; // DISPOSE, RETURN_TO_SUPPLIER, REPAIR, DISCOUNT_SALE
    private LocalDateTime reportedAt;
    private LocalDateTime processedAt;
    private String status; // PENDING, INSPECTED, PROCESSED, COMPLETED
    private Boolean refundApproved;
    private String notes;
    private Boolean success;
    private String message;
}
