
package com.example.stock_monitor.controller;

import com.example.stock_monitor.dto.StockPriceResponse;
import com.example.stock_monitor.dto.StockRequest;
import com.example.stock_monitor.model.Stock;
import com.example.stock_monitor.service.AlphaVantageMarketDataService;
import com.example.stock_monitor.service.StockService;
import com.example.stock_monitor.service.StockWebSocketService;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final AlphaVantageMarketDataService alphaVantageMarketDataService;
    private final StockWebSocketService stockWebSocketService;

    public StockController(
            StockService stockService,
            AlphaVantageMarketDataService alphaVantageMarketDataService,
            StockWebSocketService stockWebSocketService) {

        this.stockService = stockService;
        this.alphaVantageMarketDataService = alphaVantageMarketDataService;
        this.stockWebSocketService = stockWebSocketService;
    }

    // Check whether application is running
    @GetMapping("/status")
    public String status() {
        return "Stock Monitoring System is online!";
    }

    // Get all stocks from database
    @GetMapping
    public List<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    // Get stock by symbol
    @GetMapping("/{symbol}")
    public Stock getStock(@PathVariable String symbol) {
        return stockService.getStock(symbol);
    }

    // Add a stock
    @PostMapping
    public Stock addStock(@RequestBody StockRequest request) {
        return stockService.addStock(request);
    }

    // Update a stock
    @PutMapping("/{id}")
    public Stock updateStock(
            @PathVariable Long id,
            @RequestBody Stock stock) {

        return stockService.updateStock(id, stock);
    }

    // Delete a stock
    @DeleteMapping("/{id}")
    public String deleteStock(@PathVariable Long id) {

        boolean deleted = stockService.deleteStock(id);

        if (deleted) {
            return "Stock deleted successfully";
        }

        return "Stock not found";
    }

    // Get current/latest price from Alpha Vantage
    @GetMapping("/{symbol}/price")
    public StockPriceResponse getLivePrice(
            @PathVariable String symbol) {

        return alphaVantageMarketDataService.getStockPrice(symbol);
    }

    // Test WebSocket updates
    @GetMapping("/test-websocket")
    public String testWebSocket() {

        Stock testStock = new Stock(
                "TEST",
                "WebSocket Test",
                123.45,
                1.25
        );

        testStock.setChange(1.50);
        stockWebSocketService.sendStockUpdate(testStock);

        return "Test stock update sent!";
    }

    // Get daily market data directly from Alpha Vantage
    @GetMapping("/{symbol}/market-history")
    public JsonNode getMarketHistory(
            @PathVariable String symbol) {

        return alphaVantageMarketDataService
                .getDailyMarketHistory(symbol);
    }
}
