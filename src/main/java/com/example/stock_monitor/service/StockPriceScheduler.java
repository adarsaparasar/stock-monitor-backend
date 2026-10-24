
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
import java.util.Optional;

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

    // Update IBM hourly, starting one hour after startup.
    @Scheduled(fixedRate = 3600000, initialDelay = 3600000)
    @CacheEvict(value = "stocks", allEntries = true)
    public void updateStockPrices() {

        System.out.println("========== IBM STOCK UPDATE STARTED ==========");
        System.out.println("Update time: " + LocalDateTime.now());

        Optional<Stock> optionalStock =
                stockRepository.findBySymbolIgnoreCase(SYMBOL);

        if (optionalStock.isEmpty()) {
            System.out.println(
                    "IBM was not found in the database. Update skipped."
            );
            return;
        }

        Stock stock = optionalStock.get();

        try {
            double previousPrice = stock.getPrice();

            StockPriceResponse latestPrice =
                    marketDataService.getStockPrice(SYMBOL);

            double price = latestPrice.getPrice();
            double change = latestPrice.getChange();

            String percentText = latestPrice.getChangePercent()
                    .replace("%", "")
                    .trim();

            double changePercent = Double.parseDouble(percentText);
            LocalDateTime recordedAt = LocalDateTime.now();

            stock.setPrice(price);
            stock.setChange(change);
            stock.setChangePercent(changePercent);
            stockRepository.save(stock);

            if (Double.compare(previousPrice, price) != 0) {
                StockPriceHistory history = new StockPriceHistory(
                        SYMBOL,
                        price,
                        change,
                        changePercent,
                        recordedAt
                );

                historyRepository.save(history);
                System.out.println("IBM price history saved.");
            } else {
                System.out.println("IBM price unchanged; history skipped.");
            }

            webSocketService.sendStockUpdate(stock);

            System.out.printf(
                    "Updated IBM | Price: %.2f | Change: %.2f"
                            + " | Change %%: %.4f%%%n",
                    price,
                    change,
                    changePercent
            );

        } catch (Exception e) {
            System.err.println(
                    "Failed to update IBM: " + e.getMessage()
            );
        }

        System.out.println("========== IBM STOCK UPDATE FINISHED ==========");
    }

    // Manually fetch and save IBM data for testing.
    public void updateIbmPriceNow() {

        Stock stock = stockRepository.findBySymbolIgnoreCase(SYMBOL)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "IBM was not found in the database."
                        ));

        StockPriceResponse latestPrice =
                marketDataService.getStockPrice(SYMBOL);

        double price = latestPrice.getPrice();
        double change = latestPrice.getChange();

        String percentText = latestPrice.getChangePercent()
                .replace("%", "")
                .trim();

        double changePercent = Double.parseDouble(percentText);
        LocalDateTime recordedAt = LocalDateTime.now();

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

        System.out.println(
                "Manual IBM update completed | Price: " + price
                        + " | Recorded at: " + recordedAt
        );
    }
}
