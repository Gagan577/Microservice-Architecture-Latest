package com.enterprise.stock.soap;

import com.enterprise.stock.dto.BulkStockUpdateDto;
import com.enterprise.stock.dto.WarehouseStatusDto;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SOAP Service Interface for Stock Operations
 */
@WebService(targetNamespace = "http://stock.enterprise.com/soap", name = "StockSoapService")
public interface StockSoapService {

    /**
     * Use Case 4: SOAP - Bulk stock update (XML payload)
     */
    @WebMethod(operationName = "bulkStockUpdate")
    @WebResult(name = "bulkUpdateResponse")
    BulkStockUpdateDto bulkStockUpdate(
            @WebParam(name = "bulkUpdateRequest") BulkStockUpdateDto request
    );

    /**
     * Use Case 5: SOAP - Legacy warehouse status check (XML payload)
     */
    @WebMethod(operationName = "getWarehouseStatus")
    @WebResult(name = "warehouseStatusResponse")
    WarehouseStatusDto getWarehouseStatus(
            @WebParam(name = "warehouseCode") String warehouseCode
    );
}
