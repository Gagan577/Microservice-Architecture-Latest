package com.enterprise.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for stock reservation request/response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockReservationDto {
    private String reservationId;
    @NotBlank(message = "SKU is required")
    private String sku;
    @NotBlank(message = "Order ID is required")
    private String orderId;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    private String warehouseCode;
    private String customerId;
    private String status;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String notes;
    private Boolean success;
    private String message;
}
