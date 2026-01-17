package com.enterprise.shop.soap.client;

import com.enterprise.shop.dto.BulkStockUpdateDto;
import com.enterprise.shop.dto.WarehouseStatusDto;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * SOAP Client Interface for Product Stock Service
 * Handles SOAP-based inter-service communication
 */
@WebService(targetNamespace = "http://stock.enterprise.com/soap")
public interface StockSoapClient {

    /**
     * Use Case 4: SOAP - Bulk stock update (XML payload)
     */
    @WebMethod(operationName = "bulkStockUpdate")
    @WebResult(name = "bulkUpdateResponse")
    BulkStockUpdateDto bulkStockUpdate(
            @WebParam(name = "bulkUpdateRequest") @XmlElement(required = true) 
            BulkStockUpdateDto request
    );

    /**
     * Use Case 5: SOAP - Legacy warehouse status check (XML payload)
     */
    @WebMethod(operationName = "getWarehouseStatus")
    @WebResult(name = "warehouseStatusResponse")
    WarehouseStatusDto getWarehouseStatus(
            @WebParam(name = "warehouseCode") @XmlElement(required = true) 
            String warehouseCode
    );
}
