
package com.example.stock_monitor.controller;

import com.example.stock_monitor.model.StockPriceHistory;
import com.example.stock_monitor.repository.StockPriceHistoryRepository;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockPriceHistoryController {

    private final StockPriceHistoryRepository historyRepository;

    public StockPriceHistoryController(
            StockPriceHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    // Get saved stock-price history for 1H, 1D, or 1W
    @GetMapping("/{symbol}/history")
    public List<StockPriceHistory> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1D") String range) {

        LocalDateTime cutoff;
        LocalDateTime now = LocalDateTime.now();

        switch (range.toUpperCase()) {
            case "1H":
                cutoff = now.minusHours(1);
                break;

            case "1D":
                cutoff = now.minusDays(1);
                break;

            case "1W":
                cutoff = now.minusWeeks(1);
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid range. Supported values are 1H, 1D, and 1W."
                );
        }

        List<StockPriceHistory> history =
                historyRepository
                        .findBySymbolIgnoreCaseOrderByRecordedAtAsc(symbol);

        return history.stream()
                .filter(record -> record.getRecordedAt() != null)
                .filter(record ->
                        !record.getRecordedAt().isBefore(cutoff))
                .toList();
    }
}
