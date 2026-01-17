package com.enterprise.shop.client;

import com.enterprise.shop.dto.DamagedGoodsReturnDto;
import com.enterprise.shop.dto.ProductDetailsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * GraphQL Client for Product Stock Service
 * Handles GraphQL queries and mutations for inter-service communication
 */
@Component
public class StockGraphQLClient {

    private static final Logger logger = LoggerFactory.getLogger(StockGraphQLClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public StockGraphQLClient(@Qualifier("graphqlWebClient") WebClient webClient,
                              ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Use Case 6: GraphQL Query - Fetch product details + stock count + warehouse location
     */
    public ProductDetailsDto fetchProductDetails(String sku) {
        logger.info("Fetching product details via GraphQL for SKU: {}", sku);

        String query = """
            query GetProductDetails($sku: String!) {
                productDetails(sku: $sku) {
                    sku
                    productName
                    description
                    category
                    brand
                    unitPrice
                    currency
                    unitOfMeasure
                    weight
                    dimensions
                    stockCount
                    reservedCount
                    availableCount
                    stockStatus
                    warehouseCode
                    warehouseName
                    warehouseLocation
                    warehouseRegion
                    aisle
                    shelf
                    bin
                    lastStockUpdate
                    lastPriceUpdate
                    isActive
                }
            }
            """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("sku", sku);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        try {
            String response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode dataNode = jsonNode.path("data").path("productDetails");
            
            return objectMapper.treeToValue(dataNode, ProductDetailsDto.class);
        } catch (Exception e) {
            logger.error("GraphQL query failed for SKU: {}", sku, e);
            throw new RuntimeException("Failed to fetch product details: " + e.getMessage(), e);
        }
    }

    /**
     * Use Case 7: GraphQL Mutation - Register damaged goods return
     */
    public DamagedGoodsReturnDto registerDamagedReturn(DamagedGoodsReturnDto request) {
        logger.info("Registering damaged goods return via GraphQL for SKU: {}", request.getSku());

        String mutation = """
            mutation RegisterDamagedReturn($input: DamagedReturnInput!) {
                registerDamagedReturn(input: $input) {
                    returnId
                    sku
                    quantity
                    damageType
                    damageDescription
                    warehouseCode
                    reportedBy
                    inspectedBy
                    disposition
                    reportedAt
                    processedAt
                    status
                    refundApproved
                    notes
                    success
                    message
                }
            }
            """;

        Map<String, Object> input = new HashMap<>();
        input.put("sku", request.getSku());
        input.put("quantity", request.getQuantity());
        input.put("damageType", request.getDamageType());
        input.put("damageDescription", request.getDamageDescription());
        input.put("warehouseCode", request.getWarehouseCode());
        input.put("reportedBy", request.getReportedBy());
        input.put("notes", request.getNotes());

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", input);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", mutation);
        requestBody.put("variables", variables);

        try {
            String response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            
            // Check for errors
            JsonNode errorsNode = jsonNode.path("errors");
            if (!errorsNode.isMissingNode() && errorsNode.isArray() && errorsNode.size() > 0) {
                String errorMessage = errorsNode.get(0).path("message").asText();
                throw new RuntimeException("GraphQL error: " + errorMessage);
            }
            
            JsonNode dataNode = jsonNode.path("data").path("registerDamagedReturn");
            return objectMapper.treeToValue(dataNode, DamagedGoodsReturnDto.class);
        } catch (Exception e) {
            logger.error("GraphQL mutation failed for damaged return: {}", request.getSku(), e);
            throw new RuntimeException("Failed to register damaged return: " + e.getMessage(), e);
        }
    }
}
