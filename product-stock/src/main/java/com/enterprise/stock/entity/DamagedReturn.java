package com.enterprise.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Damaged Goods Return Entity - Tracks damaged goods returns
 */
@Entity
@Table(name = "damaged_returns", schema = "stock_db")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamagedReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_id", unique = true, nullable = false, length = 50)
    private String returnId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "damage_type", length = 50)
    private String damageType; // PHYSICAL, WATER, EXPIRED, DEFECTIVE, OTHER

    @Column(name = "damage_description", columnDefinition = "TEXT")
    private String damageDescription;

    @Column(name = "warehouse_code", length = 20)
    private String warehouseCode;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "inspected_by", length = 100)
    private String inspectedBy;

    @Column(length = 50)
    private String disposition; // DISPOSE, RETURN_TO_SUPPLIER, REPAIR, DISCOUNT_SALE

    @Column(length = 20)
    private String status; // PENDING, INSPECTED, PROCESSED, COMPLETED

    @Column(name = "refund_approved")
    private Boolean refundApproved;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "reported_at", updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
