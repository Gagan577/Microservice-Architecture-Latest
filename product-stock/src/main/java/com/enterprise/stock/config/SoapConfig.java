package com.enterprise.stock.config;

import com.enterprise.stock.soap.StockSoapService;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CXF/SOAP Configuration for Product Stock Service
 */
@Configuration
public class SoapConfig {

    private final Bus bus;
    private final StockSoapService stockSoapService;

    public SoapConfig(Bus bus, StockSoapService stockSoapService) {
        this.bus = bus;
        this.stockSoapService = stockSoapService;
    }

    @Bean
    public Endpoint stockSoapEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, stockSoapService);
        endpoint.publish("/stock");
        return endpoint;
    }
}
