package com.enterprise.stock.soap;

import com.enterprise.stock.dto.BulkStockUpdateDto;
import com.enterprise.stock.dto.WarehouseStatusDto;
import com.enterprise.stock.service.StockService;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SOAP Service Implementation for Stock Operations
 * Use Cases 4 & 5: Bulk stock update and warehouse status check
 */
@Service
@WebService(
        serviceName = "StockSoapService",
        portName = "StockSoapPort",
        targetNamespace = "http://stock.enterprise.com/soap",
        endpointInterface = "com.enterprise.stock.soap.StockSoapService"
)
public class StockSoapServiceImpl implements StockSoapService {

    private static final Logger logger = LoggerFactory.getLogger(StockSoapServiceImpl.class);
    private final StockService stockService;

    public StockSoapServiceImpl(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public BulkStockUpdateDto bulkStockUpdate(BulkStockUpdateDto request) {
        logger.info("SOAP: Bulk stock update request received for {} items", 
                request.getItems() != null ? request.getItems().size() : 0);
        return stockService.bulkStockUpdate(request);
    }

    @Override
    public WarehouseStatusDto getWarehouseStatus(String warehouseCode) {
        logger.info("SOAP: Warehouse status request for: {}", warehouseCode);
        return stockService.getWarehouseStatus(warehouseCode);
    }
}
