
package com.example.stock_monitor.service;

import com.example.stock_monitor.dto.StockPriceResponse;
import com.example.stock_monitor.model.Stock;
import com.example.stock_monitor.model.StockPriceHistory;
import com.example.stock_monitor.repository.StockPriceHistoryRepository;
import com.example.stock_monitor.repository.StockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockPriceScheduler {

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

    // Run every five minutes
    //@Scheduled(fixedRate = 300000)
    @CacheEvict(value = "stocks", allEntries = true)
    public void updateStockPrices() {

        System.out.println("========== STOCK UPDATE STARTED ==========");
        System.out.println("Update time: " + LocalDateTime.now());

        List<Stock> stocks = stockRepository.findAll();
        System.out.println("Stocks found: " + stocks.size());

        for (Stock stock : stocks) {
            try {
                String symbol = stock.getSymbol();
                double previousPrice = stock.getPrice();

                System.out.println("Fetching price for: " + symbol);

                StockPriceResponse latestPrice =
                        marketDataService.getStockPrice(symbol);

                double price = latestPrice.getPrice();
                double change = latestPrice.getChange();

                String percentText = latestPrice
                        .getChangePercent()
                        .replace("%", "")
                        .trim();

                double changePercent = Double.parseDouble(percentText);

                LocalDateTime recordedAt = LocalDateTime.now();

                // Update the current stock
                stock.setPrice(price);
                stock.setChange(change);
                stock.setChangePercent(changePercent);
                stockRepository.save(stock);

                // Save history only if the price has changed
                if (Double.compare(previousPrice, price) != 0) {

                    StockPriceHistory history = new StockPriceHistory(
                            symbol,
                            price,
                            change,
                            changePercent,
                            recordedAt
                    );

                    historyRepository.save(history);

                    System.out.println(
                            "New history saved for " + symbol
                                    + " | Previous: " + previousPrice
                                    + " | Current: " + price
                    );

                } else {
                    System.out.println(
                            "Price unchanged for " + symbol
                                    + " | Price: " + price
                                    + " | Duplicate history skipped"
                    );
                }

                // Broadcast the latest stock data to React
                webSocketService.sendStockUpdate(stock);

                System.out.printf(
                        "Updated %s | Price: %.2f | Change: %.2f"
                                + " | Change %%: %.4f%%%n",
                        symbol,
                        price,
                        change,
                        changePercent
                );

            } catch (Exception e) {
                System.err.println(
                        "Failed to update " + stock.getSymbol()
                                + " | Error: " + e.getMessage()
                );
            }
        }

        System.out.println("========== STOCK UPDATE FINISHED ==========");
    }
}
