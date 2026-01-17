package com.enterprise.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Warehouse Entity - Warehouse information
 */
@Entity
@Table(name = "warehouses", schema = "stock_db")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", unique = true, nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String warehouseName;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String region;

    @Column(length = 20)
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, CLOSED

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(name = "used_capacity")
    private Integer usedCapacity;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "last_inventory_check")
    private LocalDateTime lastInventoryCheck;

    @Column(name = "is_operational")
    private Boolean isOperational;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
