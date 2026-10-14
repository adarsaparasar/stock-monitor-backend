
        package com.example.stock_monitor.service;

        import com.example.stock_monitor.dto.StockPriceResponse;
        import com.fasterxml.jackson.databind.JsonNode;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.cache.annotation.Cacheable;
        import org.springframework.stereotype.Service;
        import org.springframework.web.client.RestClient;

        @Service
public class AlphaVantageMarketDataService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AlphaVantageMarketDataService(
            @Value("${alphavantage.api.base-url}") String baseUrl,
            @Value("${alphavantage.api.key}") String apiKey,
            ObjectMapper objectMapper) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch the latest quote for a stock using GLOBAL_QUOTE.
     */
    public StockPriceResponse getStockPrice(String symbol) {

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new StockDataNotFoundException(
                        "Empty response received for symbol: " + symbol);
            }

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Information") || root.has("Note")) {
                String message = root.has("Information")
                        ? root.get("Information").asText()
                        : root.get("Note").asText();

                throw new AlphaVantageRateLimitException(
                        "Alpha Vantage API notice: " + message);
            }

            if (root.has("Error Message")) {
                throw new StockDataNotFoundException(
                        root.get("Error Message").asText());
            }

            JsonNode quote = root.path("Global Quote");

            String stockSymbol = quote.path("01. symbol").asText();
            String priceText = quote.path("05. price").asText();
            String changeText = quote.path("09. change").asText();
            String changePercent = quote.path("10. change percent").asText();

            if (stockSymbol.isBlank()
                    || priceText.isBlank()
                    || changeText.isBlank()
                    || changePercent.isBlank()) {

                throw new StockDataNotFoundException(
                        "Incomplete stock quote received for: " + symbol);
            }

            double price = Double.parseDouble(priceText);
            double change = Double.parseDouble(changeText);

            return new StockPriceResponse(
                    stockSymbol,
                    price,
                    change,
                    changePercent
            );

        } catch (StockDataNotFoundException
                 | AlphaVantageRateLimitException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to retrieve stock quote for: " + symbol, e);
        }
    }

    /**
     * Fetch daily historical prices.
     * Successful responses are cached in Redis for 24 hours.
     */
    @Cacheable(
            value = "dailyMarketHistory",
            key = "#symbol.toUpperCase()"
    )
    public JsonNode getDailyMarketHistory(String symbol) {

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "TIME_SERIES_DAILY")
                            .queryParam("symbol", symbol)
                            .queryParam("outputsize", "compact")
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new StockDataNotFoundException(
                        "Empty daily history response for: " + symbol);
            }

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Information") || root.has("Note")) {
                String message = root.has("Information")
                        ? root.get("Information").asText()
                        : root.get("Note").asText();

                throw new AlphaVantageRateLimitException(
                        "Alpha Vantage API notice: " + message);
            }

            if (root.has("Error Message")) {
                throw new StockDataNotFoundException(
                        root.get("Error Message").asText());
            }

            JsonNode timeSeries = root.path("Time Series (Daily)");

            if (timeSeries.isMissingNode()
                    || timeSeries.isNull()
                    || timeSeries.isEmpty()) {

                throw new StockDataNotFoundException(
                        "Daily market history not found for: " + symbol);
            }

            return timeSeries;

        } catch (StockDataNotFoundException
                 | AlphaVantageRateLimitException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to retrieve daily market history for: "
                            + symbol, e);
        }
    }
}
