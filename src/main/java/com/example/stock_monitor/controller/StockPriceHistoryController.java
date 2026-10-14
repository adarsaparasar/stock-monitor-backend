
package com.example.stock_monitor.controller;

import com.example.stock_monitor.model.StockPriceHistory;
import com.example.stock_monitor.repository.StockPriceHistoryRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockPriceHistoryController {

    private final StockPriceHistoryRepository historyRepository;

    public StockPriceHistoryController(
            StockPriceHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping("/{symbol}/history")
    public List<StockPriceHistory> getPriceHistory(
            @PathVariable String symbol) {

        return historyRepository
                .findBySymbolIgnoreCaseOrderByRecordedAtAsc(symbol);
    }
}
