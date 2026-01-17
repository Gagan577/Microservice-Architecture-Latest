package com.enterprise.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Entity - Core product information
 */
@Entity
@Table(name = "products", schema = "stock_db")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 10)
    private String currency;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    private Double weight;

    @Column(length = 100)
    private String dimensions;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "discontinued_at")
    private LocalDateTime discontinuedAt;

    @Column(name = "discontinued_reason")
    private String discontinuedReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
