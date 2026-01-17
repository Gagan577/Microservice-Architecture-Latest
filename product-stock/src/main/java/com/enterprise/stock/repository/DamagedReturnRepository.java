package com.enterprise.stock.repository;

import com.enterprise.stock.entity.DamagedReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DamagedReturnRepository extends JpaRepository<DamagedReturn, Long> {
    
    Optional<DamagedReturn> findByReturnId(String returnId);
    
    List<DamagedReturn> findBySkuAndStatus(String sku, String status);
    
    List<DamagedReturn> findByWarehouseCode(String warehouseCode);
}
