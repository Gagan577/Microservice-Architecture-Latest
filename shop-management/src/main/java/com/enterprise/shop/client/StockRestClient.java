package com.enterprise.shop.client;

import com.enterprise.shop.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * REST Client for Product Stock Service
 * Handles all REST-based inter-service communication
 */
@Component
public class StockRestClient {

    private static final Logger logger = LoggerFactory.getLogger(StockRestClient.class);
    private final WebClient webClient;

    public StockRestClient(@Qualifier("stockServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Use Case 1: REST GET - Check item availability
     */
    public StockAvailabilityDto checkAvailability(String sku) {
        logger.info("Checking availability for SKU: {}", sku);
        
        return webClient.get()
                .uri("/api/stock/availability/{sku}", sku)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                        Mono.error(new RuntimeException("Stock service error: " + response.statusCode())))
                .bodyToMono(StockAvailabilityDto.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .block();
    }

    /**
     * Use Case 2: REST POST - Reserve stock for an order
     */
    public StockReservationDto reserveStock(StockReservationDto reservation) {
        logger.info("Reserving stock for SKU: {}, Quantity: {}", 
                reservation.getSku(), reservation.getQuantity());
        
        return webClient.post()
                .uri("/api/stock/reservations")
                .bodyValue(reservation)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Reservation failed: " + body))))
                .bodyToMono(StockReservationDto.class)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                .block();
    }

    /**
     * Use Case 3: REST PUT - Update stock threshold
     */
    public StockThresholdDto updateThreshold(String sku, StockThresholdDto threshold) {
        logger.info("Updating threshold for SKU: {}", sku);
        
        return webClient.put()
                .uri("/api/stock/thresholds/{sku}", sku)
                .bodyValue(threshold)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Threshold update failed")))
                .bodyToMono(StockThresholdDto.class)
                .block();
    }

    /**
     * Use Case 8: REST PATCH - Update price adjustments
     */
    public PriceAdjustmentDto adjustPrice(String sku, PriceAdjustmentDto adjustment) {
        logger.info("Adjusting price for SKU: {}", sku);
        
        return webClient.patch()
                .uri("/api/stock/products/{sku}/price", sku)
                .bodyValue(adjustment)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Price adjustment failed")))
                .bodyToMono(PriceAdjustmentDto.class)
                .block();
    }

    /**
     * Use Case 9: REST DELETE - Discontinue a product SKU
     */
    public ProductDiscontinueDto discontinueProduct(String sku, ProductDiscontinueDto request) {
        logger.info("Discontinuing product SKU: {}", sku);
        
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/stock/products/{sku}")
                        .queryParam("reason", request.getReason())
                        .queryParam("disposition", request.getStockDisposition())
                        .build(sku))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Product discontinuation failed")))
                .bodyToMono(ProductDiscontinueDto.class)
                .block();
    }

    /**
     * Use Case 10: REST GET Complex - Search stock with pagination and filtering
     */
    public StockSearchResponseDto searchStock(StockSearchRequestDto request) {
        logger.info("Searching stock with filters: {}", request);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/stock/search");
                    
                    if (request.getSku() != null) 
                        uriBuilder.queryParam("sku", request.getSku());
                    if (request.getProductName() != null) 
                        uriBuilder.queryParam("productName", request.getProductName());
                    if (request.getCategory() != null) 
                        uriBuilder.queryParam("category", request.getCategory());
                    if (request.getWarehouseCode() != null) 
                        uriBuilder.queryParam("warehouseCode", request.getWarehouseCode());
                    if (request.getStockStatus() != null) 
                        uriBuilder.queryParam("stockStatus", request.getStockStatus());
                    if (request.getMinQuantity() != null) 
                        uriBuilder.queryParam("minQuantity", request.getMinQuantity());
                    if (request.getMaxQuantity() != null) 
                        uriBuilder.queryParam("maxQuantity", request.getMaxQuantity());
                    if (request.getSortBy() != null) 
                        uriBuilder.queryParam("sortBy", request.getSortBy());
                    if (request.getSortDirection() != null) 
                        uriBuilder.queryParam("sortDirection", request.getSortDirection());
                    if (request.getPage() != null) 
                        uriBuilder.queryParam("page", request.getPage());
                    if (request.getSize() != null) 
                        uriBuilder.queryParam("size", request.getSize());
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Stock search failed")))
                .bodyToMono(StockSearchResponseDto.class)
                .block();
    }
}
