package com.enterprise.stock.service;

import com.enterprise.stock.dto.*;
import com.enterprise.stock.entity.*;
import com.enterprise.stock.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stock Service - Handles all stock-related business logic
 */
@Service
@Transactional
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockReservationRepository reservationRepository;
    private final DamagedReturnRepository damagedReturnRepository;

    public StockService(ProductRepository productRepository,
                        StockRepository stockRepository,
                        WarehouseRepository warehouseRepository,
                        StockReservationRepository reservationRepository,
                        DamagedReturnRepository damagedReturnRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
        this.reservationRepository = reservationRepository;
        this.damagedReturnRepository = damagedReturnRepository;
    }

    // ==========================================================================
    // Use Case 1: Check item availability
    // ==========================================================================
    @Transactional(readOnly = true)
    public StockAvailabilityDto checkAvailability(String sku) {
        logger.info("Checking availability for SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku).orElse(null);
        if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
            return StockAvailabilityDto.builder()
                    .sku(sku)
                    .isAvailable(false)
                    .status("NOT_FOUND")
                    .message("Product not found or inactive")
                    .build();
        }

        Integer totalStock = stockRepository.getTotalStockBySku(sku);
        Integer totalReserved = stockRepository.getTotalReservedBySku(sku);
        
        totalStock = totalStock != null ? totalStock : 0;
        totalReserved = totalReserved != null ? totalReserved : 0;
        int availableQuantity = totalStock - totalReserved;

        Stock primaryStock = stockRepository.findBySku(sku).stream().findFirst().orElse(null);

        return StockAvailabilityDto.builder()
                .sku(sku)
                .productName(product.getProductName())
                .availableQuantity(availableQuantity)
                .reservedQuantity(totalReserved)
                .warehouseCode(primaryStock != null ? primaryStock.getWarehouseCode() : null)
                .isAvailable(availableQuantity > 0)
                .lastUpdated(primaryStock != null ? primaryStock.getUpdatedAt() : null)
                .status(availableQuantity > 0 ? "IN_STOCK" : "OUT_OF_STOCK")
                .message("Availability check successful")
                .build();
    }

    // ==========================================================================
    // Use Case 2: Reserve stock for an order
    // ==========================================================================
    public StockReservationDto reserveStock(StockReservationDto request) {
        logger.info("Reserving stock for SKU: {}, Order: {}", request.getSku(), request.getOrderId());
        
        // Check availability
        StockAvailabilityDto availability = checkAvailability(request.getSku());
        if (!Boolean.TRUE.equals(availability.getIsAvailable()) || 
            availability.getAvailableQuantity() < request.getQuantity()) {
            return StockReservationDto.builder()
                    .sku(request.getSku())
                    .orderId(request.getOrderId())
                    .quantity(request.getQuantity())
                    .success(false)
                    .status("FAILED")
                    .message("Insufficient stock available. Requested: " + request.getQuantity() + 
                            ", Available: " + availability.getAvailableQuantity())
                    .build();
        }

        // Find stock to reserve from
        Stock stock = stockRepository.findBySku(request.getSku()).stream()
                .filter(s -> s.getQuantity() - (s.getReservedQuantity() != null ? s.getReservedQuantity() : 0) >= request.getQuantity())
                .findFirst()
                .orElse(null);

        if (stock == null) {
            return StockReservationDto.builder()
                    .sku(request.getSku())
                    .orderId(request.getOrderId())
                    .success(false)
                    .status("FAILED")
                    .message("No warehouse has sufficient stock")
                    .build();
        }

        // Update stock reservation
        stock.setReservedQuantity((stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0) + request.getQuantity());
        stockRepository.save(stock);

        // Create reservation record
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        StockReservation reservation = StockReservation.builder()
                .reservationId(reservationId)
                .sku(request.getSku())
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .warehouseCode(stock.getWarehouseCode())
                .customerId(request.getCustomerId())
                .status("CONFIRMED")
                .notes(request.getNotes())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .confirmedAt(LocalDateTime.now())
                .build();
        reservationRepository.save(reservation);

        return StockReservationDto.builder()
                .reservationId(reservationId)
                .sku(request.getSku())
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .warehouseCode(stock.getWarehouseCode())
                .customerId(request.getCustomerId())
                .status("CONFIRMED")
                .reservedAt(reservation.getReservedAt())
                .expiresAt(reservation.getExpiresAt())
                .success(true)
                .message("Stock reserved successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 3: Update stock threshold
    // ==========================================================================
    public StockThresholdDto updateThreshold(String sku, StockThresholdDto request) {
        logger.info("Updating threshold for SKU: {}", sku);
        
        List<Stock> stocks = stockRepository.findBySku(sku);
        if (stocks.isEmpty()) {
            return StockThresholdDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("No stock found for SKU: " + sku)
                    .build();
        }

        for (Stock stock : stocks) {
            if (request.getWarehouseCode() == null || 
                stock.getWarehouseCode().equals(request.getWarehouseCode())) {
                stock.setMinThreshold(request.getMinThreshold());
                stock.setMaxThreshold(request.getMaxThreshold());
                stock.setReorderPoint(request.getReorderPoint());
                stock.setReorderQuantity(request.getReorderQuantity());
                stock.setAutoReorder(request.getAutoReorder());
                stockRepository.save(stock);
            }
        }

        return StockThresholdDto.builder()
                .sku(sku)
                .minThreshold(request.getMinThreshold())
                .maxThreshold(request.getMaxThreshold())
                .reorderPoint(request.getReorderPoint())
                .reorderQuantity(request.getReorderQuantity())
                .warehouseCode(request.getWarehouseCode())
                .autoReorder(request.getAutoReorder())
                .success(true)
                .message("Threshold updated successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 4: Bulk stock update (SOAP)
    // ==========================================================================
    public BulkStockUpdateDto bulkStockUpdate(BulkStockUpdateDto request) {
        logger.info("Processing bulk stock update for {} items", 
                request.getItems() != null ? request.getItems().size() : 0);
        
        String batchId = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<BulkStockUpdateDto.UpdateResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return BulkStockUpdateDto.builder()
                    .batchId(batchId)
                    .status("FAILED")
                    .message("No items to update")
                    .totalItems(0)
                    .successCount(0)
                    .failureCount(0)
                    .build();
        }

        for (BulkStockUpdateDto.StockItemUpdate item : request.getItems()) {
            try {
                Stock stock = stockRepository.findBySkuAndWarehouseCode(
                        item.getSku(), 
                        request.getWarehouseCode() != null ? request.getWarehouseCode() : "DEFAULT"
                ).orElse(null);

                int previousQuantity = 0;
                int newQuantity = 0;

                if (stock == null) {
                    // Create new stock entry
                    stock = Stock.builder()
                            .sku(item.getSku())
                            .warehouseCode(request.getWarehouseCode() != null ? request.getWarehouseCode() : "DEFAULT")
                            .quantity(0)
                            .reservedQuantity(0)
                            .minThreshold(10)
                            .maxThreshold(1000)
                            .build();
                }

                previousQuantity = stock.getQuantity() != null ? stock.getQuantity() : 0;

                switch (item.getOperation() != null ? item.getOperation().toUpperCase() : "SET") {
                    case "ADD":
                        newQuantity = previousQuantity + item.getQuantity();
                        break;
                    case "REMOVE":
                        newQuantity = Math.max(0, previousQuantity - item.getQuantity());
                        break;
                    case "SET":
                    default:
                        newQuantity = item.getQuantity();
                        break;
                }

                stock.setQuantity(newQuantity);
                stockRepository.save(stock);

                results.add(BulkStockUpdateDto.UpdateResult.builder()
                        .sku(item.getSku())
                        .success(true)
                        .message("Updated successfully")
                        .previousQuantity(previousQuantity)
                        .newQuantity(newQuantity)
                        .build());
                successCount++;

            } catch (Exception e) {
                results.add(BulkStockUpdateDto.UpdateResult.builder()
                        .sku(item.getSku())
                        .success(false)
                        .message("Update failed: " + e.getMessage())
                        .build());
                failureCount++;
            }
        }

        return BulkStockUpdateDto.builder()
                .batchId(batchId)
                .warehouseCode(request.getWarehouseCode())
                .totalItems(request.getItems().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .status(failureCount == 0 ? "COMPLETED" : "PARTIAL")
                .message(String.format("Bulk update completed. Success: %d, Failed: %d", successCount, failureCount))
                .results(results)
                .build();
    }

    // ==========================================================================
    // Use Case 5: Warehouse status check (SOAP)
    // ==========================================================================
    @Transactional(readOnly = true)
    public WarehouseStatusDto getWarehouseStatus(String warehouseCode) {
        logger.info("Getting status for warehouse: {}", warehouseCode);
        
        Warehouse warehouse = warehouseRepository.findByWarehouseCode(warehouseCode).orElse(null);
        if (warehouse == null) {
            return WarehouseStatusDto.builder()
                    .warehouseCode(warehouseCode)
                    .isOperational(false)
                    .message("Warehouse not found")
                    .build();
        }

        Integer totalSkus = stockRepository.countSkusByWarehouse(warehouseCode);
        Integer lowStockSkus = stockRepository.countLowStockByWarehouse(warehouseCode);
        Integer outOfStockSkus = stockRepository.countOutOfStockByWarehouse(warehouseCode);

        int availableCapacity = warehouse.getTotalCapacity() != null && warehouse.getUsedCapacity() != null
                ? warehouse.getTotalCapacity() - warehouse.getUsedCapacity()
                : 0;
        double utilizationPercentage = warehouse.getTotalCapacity() != null && warehouse.getTotalCapacity() > 0
                ? (warehouse.getUsedCapacity() != null ? warehouse.getUsedCapacity() : 0) * 100.0 / warehouse.getTotalCapacity()
                : 0.0;

        return WarehouseStatusDto.builder()
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .location(warehouse.getLocation())
                .region(warehouse.getRegion())
                .status(warehouse.getStatus())
                .totalCapacity(warehouse.getTotalCapacity())
                .usedCapacity(warehouse.getUsedCapacity())
                .availableCapacity(availableCapacity)
                .utilizationPercentage(utilizationPercentage)
                .totalSkus(totalSkus != null ? totalSkus : 0)
                .lowStockSkus(lowStockSkus != null ? lowStockSkus : 0)
                .outOfStockSkus(outOfStockSkus != null ? outOfStockSkus : 0)
                .lastInventoryCheck(warehouse.getLastInventoryCheck())
                .lastUpdated(LocalDateTime.now())
                .contactPerson(warehouse.getContactPerson())
                .contactEmail(warehouse.getContactEmail())
                .contactPhone(warehouse.getContactPhone())
                .isOperational(warehouse.getIsOperational())
                .message("Status retrieved successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 6: Fetch product details (GraphQL)
    // ==========================================================================
    @Transactional(readOnly = true)
    public ProductDetailsDto getProductDetails(String sku) {
        logger.info("Fetching product details for SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku).orElse(null);
        if (product == null) {
            return ProductDetailsDto.builder()
                    .sku(sku)
                    .message("Product not found")
                    .build();
        }

        Stock stock = stockRepository.findBySku(sku).stream().findFirst().orElse(null);
        Warehouse warehouse = stock != null 
                ? warehouseRepository.findByWarehouseCode(stock.getWarehouseCode()).orElse(null)
                : null;

        Integer stockCount = stockRepository.getTotalStockBySku(sku);
        Integer reservedCount = stockRepository.getTotalReservedBySku(sku);
        stockCount = stockCount != null ? stockCount : 0;
        reservedCount = reservedCount != null ? reservedCount : 0;

        return ProductDetailsDto.builder()
                .sku(product.getSku())
                .productName(product.getProductName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .unitPrice(product.getUnitPrice())
                .currency(product.getCurrency())
                .unitOfMeasure(product.getUnitOfMeasure())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .stockCount(stockCount)
                .reservedCount(reservedCount)
                .availableCount(stockCount - reservedCount)
                .stockStatus(stock != null ? stock.getStockStatus() : "UNKNOWN")
                .warehouseCode(stock != null ? stock.getWarehouseCode() : null)
                .warehouseName(warehouse != null ? warehouse.getWarehouseName() : null)
                .warehouseLocation(warehouse != null ? warehouse.getLocation() : null)
                .warehouseRegion(warehouse != null ? warehouse.getRegion() : null)
                .aisle(stock != null ? stock.getAisle() : null)
                .shelf(stock != null ? stock.getShelf() : null)
                .bin(stock != null ? stock.getBin() : null)
                .lastStockUpdate(stock != null ? stock.getUpdatedAt() : null)
                .lastPriceUpdate(product.getUpdatedAt())
                .isActive(product.getIsActive())
                .message("Product details retrieved successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 7: Register damaged goods return (GraphQL)
    // ==========================================================================
    public DamagedGoodsReturnDto registerDamagedReturn(DamagedGoodsReturnDto request) {
        logger.info("Registering damaged return for SKU: {}", request.getSku());
        
        String returnId = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        DamagedReturn damagedReturn = DamagedReturn.builder()
                .returnId(returnId)
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .damageType(request.getDamageType())
                .damageDescription(request.getDamageDescription())
                .warehouseCode(request.getWarehouseCode())
                .reportedBy(request.getReportedBy())
                .status("PENDING")
                .notes(request.getNotes())
                .build();
        
        damagedReturnRepository.save(damagedReturn);

        // Update stock quantity
        if (request.getWarehouseCode() != null) {
            Stock stock = stockRepository.findBySkuAndWarehouseCode(
                    request.getSku(), request.getWarehouseCode()).orElse(null);
            if (stock != null) {
                stock.setQuantity(Math.max(0, stock.getQuantity() - request.getQuantity()));
                stockRepository.save(stock);
            }
        }

        return DamagedGoodsReturnDto.builder()
                .returnId(returnId)
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .damageType(request.getDamageType())
                .damageDescription(request.getDamageDescription())
                .warehouseCode(request.getWarehouseCode())
                .reportedBy(request.getReportedBy())
                .reportedAt(damagedReturn.getReportedAt())
                .status("PENDING")
                .notes(request.getNotes())
                .success(true)
                .message("Damaged return registered successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 8: Update price adjustments
    // ==========================================================================
    public PriceAdjustmentDto adjustPrice(String sku, PriceAdjustmentDto request) {
        logger.info("Adjusting price for SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku).orElse(null);
        if (product == null) {
            return PriceAdjustmentDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("Product not found")
                    .build();
        }

        BigDecimal currentPrice = product.getUnitPrice();
        product.setUnitPrice(request.getNewPrice());
        productRepository.save(product);

        return PriceAdjustmentDto.builder()
                .sku(sku)
                .currentPrice(currentPrice)
                .newPrice(request.getNewPrice())
                .discountPercentage(request.getDiscountPercentage())
                .adjustmentReason(request.getAdjustmentReason())
                .adjustedBy(request.getAdjustedBy())
                .success(true)
                .message("Price adjusted successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 9: Discontinue product
    // ==========================================================================
    public ProductDiscontinueDto discontinueProduct(String sku, ProductDiscontinueDto request) {
        logger.info("Discontinuing product SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku).orElse(null);
        if (product == null) {
            return ProductDiscontinueDto.builder()
                    .sku(sku)
                    .success(false)
                    .message("Product not found")
                    .build();
        }

        Integer remainingQuantity = stockRepository.getTotalStockBySku(sku);
        remainingQuantity = remainingQuantity != null ? remainingQuantity : 0;

        product.setIsActive(false);
        product.setDiscontinuedAt(LocalDateTime.now());
        product.setDiscontinuedReason(request.getReason());
        productRepository.save(product);

        return ProductDiscontinueDto.builder()
                .sku(sku)
                .reason(request.getReason())
                .discontinuedBy(request.getDiscontinuedBy())
                .effectiveDate(LocalDateTime.now().toString())
                .stockDisposition(request.getStockDisposition())
                .remainingQuantity(remainingQuantity)
                .success(true)
                .message("Product discontinued successfully")
                .build();
    }

    // ==========================================================================
    // Use Case 10: Search stock with pagination
    // ==========================================================================
    @Transactional(readOnly = true)
    public StockSearchResponseDto searchStock(StockSearchRequestDto request) {
        logger.info("Searching stock with filters");
        
        long startTime = System.currentTimeMillis();

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = request.getSortBy() != null ? request.getSortBy() : "sku";
        
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                Sort.by(direction, sortField)
        );

        Page<Stock> stockPage = stockRepository.searchStock(
                request.getSku(),
                request.getWarehouseCode(),
                request.getStockStatus(),
                request.getMinQuantity(),
                request.getMaxQuantity(),
                pageable
        );

        List<StockSearchResponseDto.StockSearchItem> items = stockPage.getContent().stream()
                .map(stock -> {
                    Product product = productRepository.findBySku(stock.getSku()).orElse(null);
                    Warehouse warehouse = warehouseRepository.findByWarehouseCode(stock.getWarehouseCode()).orElse(null);
                    
                    return StockSearchResponseDto.StockSearchItem.builder()
                            .sku(stock.getSku())
                            .productName(product != null ? product.getProductName() : "Unknown")
                            .category(product != null ? product.getCategory() : null)
                            .quantity(stock.getQuantity())
                            .reservedQuantity(stock.getReservedQuantity())
                            .availableQuantity(stock.getQuantity() - (stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0))
                            .warehouseCode(stock.getWarehouseCode())
                            .warehouseLocation(warehouse != null ? warehouse.getLocation() : null)
                            .stockStatus(stock.getStockStatus())
                            .lastUpdated(stock.getUpdatedAt() != null ? stock.getUpdatedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());

        long searchTimeMs = System.currentTimeMillis() - startTime;

        return StockSearchResponseDto.builder()
                .items(items)
                .pagination(StockSearchResponseDto.PaginationInfo.builder()
                        .currentPage(stockPage.getNumber())
                        .pageSize(stockPage.getSize())
                        .totalElements(stockPage.getTotalElements())
                        .totalPages(stockPage.getTotalPages())
                        .hasNext(stockPage.hasNext())
                        .hasPrevious(stockPage.hasPrevious())
                        .build())
                .metadata(StockSearchResponseDto.SearchMetadata.builder()
                        .sortBy(sortField)
                        .sortDirection(direction.name())
                        .searchTimeMs(searchTimeMs)
                        .build())
                .build();
    }
}
