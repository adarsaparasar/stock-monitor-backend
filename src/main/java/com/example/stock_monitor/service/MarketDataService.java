package com.example.stock_monitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MarketDataService {

    private final RestClient restClient;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    @Value("${alphavantage.api.base-url}")
    private String baseUrl;

    public MarketDataService() {
        this.restClient = RestClient.builder().build();
    }

    public String getStockPrice(String symbol) {

        String url = baseUrl
                + "/query?function=GLOBAL_QUOTE"
                + "&symbol=" + symbol
                + "&apikey=" + apiKey;

        return restClient
                .get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

}