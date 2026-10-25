
package com.example.stock_monitor.service;

import com.example.stock_monitor.dto.StockPriceResponse;
import com.example.stock_monitor.model.Stock;
import com.example.stock_monitor.model.StockPriceHistory;
import com.example.stock_monitor.repository.StockPriceHistoryRepository;
import com.example.stock_monitor.repository.StockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StockPriceScheduler {

    private static final String SYMBOL = "IBM";

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;
    private final AlphaVantageMarketDataService marketDataService;
    private final StockWebSocketService webSocketService;

    public StockPriceScheduler(
            StockRepository stockRepository,
            StockPriceHistoryRepository historyRepository,
            AlphaVantageMarketDataService marketDataService,
            StockWebSocketService webSocketService) {
        this.stockRepository = stockRepository;
        this.historyRepository = historyRepository;
        this.marketDataService = marketDataService;
        this.webSocketService = webSocketService;
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 3600000)
    @CacheEvict(value = "stocks", allEntries = true)
    public void updateStockPrices() {
        System.out.println(
                "========== IBM STOCK UPDATE STARTED ==========");

        try {
            updateIbmPriceNow();
        } catch (Exception e) {
            System.err.println(
                    "Failed to update IBM: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(
                "========== IBM STOCK UPDATE FINISHED ==========");
    }

    public void updateIbmPriceNow() {
        Stock stock = stockRepository.findBySymbolIgnoreCase(SYMBOL)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "IBM was not found in the database."));

        StockPriceResponse latestPrice =
                marketDataService.getStockPrice(SYMBOL);

        if (latestPrice == null || latestPrice.getPrice() <= 0) {
            throw new IllegalStateException(
                    "Alpha Vantage returned an invalid IBM price.");
        }

        double price = latestPrice.getPrice();
        double change = latestPrice.getChange();

        String changePercentText = latestPrice.getChangePercent()
                .replace("%", "")
                .trim();

        double changePercent = Double.parseDouble(changePercentText);

        LocalDateTime recordedAt = LocalDateTime.now();

        // Store the quote and its reported daily change.
        stock.setPrice(price);
        stock.setChange(change);
        stock.setChangePercent(changePercent);
        stockRepository.save(stock);

        StockPriceHistory history = new StockPriceHistory(
                SYMBOL,
                price,
                change,
                changePercent,
                recordedAt
        );

        historyRepository.save(history);
        webSocketService.sendStockUpdate(stock);

        System.out.printf(
                "Updated IBM | Price: %.2f | Change: %.2f | Change %%: %.4f%%%n",
                price,
                change,
                changePercent
        );
    }
}
