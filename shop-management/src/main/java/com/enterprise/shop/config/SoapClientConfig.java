package com.enterprise.shop.config;

import com.enterprise.shop.soap.client.StockSoapClient;
import jakarta.xml.ws.BindingProvider;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * SOAP Client Configuration for calling product-stock SOAP endpoints
 */
@Configuration
public class SoapClientConfig {

    @Value("${stock-service.base-url}")
    private String stockServiceBaseUrl;

    @Value("${stock-service.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${stock-service.connection.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public StockSoapClient stockSoapClient() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(StockSoapClient.class);
        factory.setAddress(stockServiceBaseUrl + "/ws/stock");
        
        StockSoapClient client = (StockSoapClient) factory.create();
        
        // Configure timeouts
        BindingProvider bindingProvider = (BindingProvider) client;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put("javax.xml.ws.client.connectionTimeout", connectionTimeout);
        requestContext.put("javax.xml.ws.client.receiveTimeout", readTimeout);
        
        return client;
    }
}
