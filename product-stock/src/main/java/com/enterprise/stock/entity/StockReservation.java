package com.enterprise.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stock Reservation Entity - Tracks stock reservations
 */
@Entity
@Table(name = "stock_reservations", schema = "stock_db")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", unique = true, nullable = false, length = 50)
    private String reservationId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "warehouse_code", length = 20)
    private String warehouseCode;

    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(length = 20)
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "reserved_at", updatable = false)
    private LocalDateTime reservedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
