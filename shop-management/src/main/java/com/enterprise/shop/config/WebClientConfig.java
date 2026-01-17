package com.enterprise.shop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient Configuration for inter-service communication
 * Configures REST client for calling product-stock service
 */
@Configuration
public class WebClientConfig {

    @Value("${stock-service.base-url}")
    private String stockServiceBaseUrl;

    @Value("${stock-service.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${stock-service.connection.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    @Bean("stockServiceWebClient")
    public WebClient stockServiceWebClient(ObjectMapper objectMapper) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .build();

        return WebClient.builder()
                .baseUrl(stockServiceBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("graphqlWebClient")
    public WebClient graphqlWebClient(ObjectMapper objectMapper) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .build();

        return WebClient.builder()
                .baseUrl(stockServiceBaseUrl + "/graphql")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
