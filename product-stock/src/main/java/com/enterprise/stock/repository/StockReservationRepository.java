package com.enterprise.stock.repository;

import com.enterprise.stock.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    
    Optional<StockReservation> findByReservationId(String reservationId);
    
    List<StockReservation> findByOrderId(String orderId);
    
    List<StockReservation> findBySkuAndStatus(String sku, String status);
}
