package com.enterprise.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stock Entity - Inventory levels per product per warehouse
 */
@Entity
@Table(name = "stock", schema = "stock_db",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sku", "warehouse_code"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Column(name = "min_threshold")
    private Integer minThreshold;

    @Column(name = "max_threshold")
    private Integer maxThreshold;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "auto_reorder")
    private Boolean autoReorder;

    @Column(length = 20)
    private String aisle;

    @Column(length = 20)
    private String shelf;

    @Column(length = 20)
    private String bin;

    @Column(name = "stock_status", length = 20)
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK

    @Column(name = "last_inventory_check")
    private LocalDateTime lastInventoryCheck;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateStockStatus() {
        int available = (quantity != null ? quantity : 0) - (reservedQuantity != null ? reservedQuantity : 0);
        if (available <= 0) {
            this.stockStatus = "OUT_OF_STOCK";
        } else if (minThreshold != null && available <= minThreshold) {
            this.stockStatus = "LOW_STOCK";
        } else {
            this.stockStatus = "IN_STOCK";
        }
    }
}
