package com.enterprise.stock.repository;

import com.enterprise.stock.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
           "(:category IS NULL OR p.category = :category)")
    Page<Product> searchProducts(
            @Param("sku") String sku,
            @Param("productName") String productName,
            @Param("category") String category,
            Pageable pageable);
}
