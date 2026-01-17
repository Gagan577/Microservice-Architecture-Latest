package com.enterprise.stock.repository;

import com.enterprise.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    Optional<Stock> findBySkuAndWarehouseCode(String sku, String warehouseCode);
    
    List<Stock> findBySku(String sku);
    
    List<Stock> findByWarehouseCode(String warehouseCode);
    
    @Query("SELECT s FROM Stock s WHERE " +
           "(:sku IS NULL OR LOWER(s.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:warehouseCode IS NULL OR s.warehouseCode = :warehouseCode) AND " +
           "(:stockStatus IS NULL OR s.stockStatus = :stockStatus) AND " +
           "(:minQuantity IS NULL OR s.quantity >= :minQuantity) AND " +
           "(:maxQuantity IS NULL OR s.quantity <= :maxQuantity)")
    Page<Stock> searchStock(
            @Param("sku") String sku,
            @Param("warehouseCode") String warehouseCode,
            @Param("stockStatus") String stockStatus,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity,
            Pageable pageable);
    
    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.sku = :sku")
    Integer getTotalStockBySku(@Param("sku") String sku);
    
    @Query("SELECT SUM(s.reservedQuantity) FROM Stock s WHERE s.sku = :sku")
    Integer getTotalReservedBySku(@Param("sku") String sku);
    
    @Query("SELECT COUNT(DISTINCT s.sku) FROM Stock s WHERE s.warehouseCode = :warehouseCode")
    Integer countSkusByWarehouse(@Param("warehouseCode") String warehouseCode);
    
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.warehouseCode = :warehouseCode AND s.stockStatus = 'LOW_STOCK'")
    Integer countLowStockByWarehouse(@Param("warehouseCode") String warehouseCode);
    
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.warehouseCode = :warehouseCode AND s.stockStatus = 'OUT_OF_STOCK'")
    Integer countOutOfStockByWarehouse(@Param("warehouseCode") String warehouseCode);
}
