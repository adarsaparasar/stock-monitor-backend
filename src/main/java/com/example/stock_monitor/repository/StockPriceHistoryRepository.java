package com.example.stock_monitor.repository;

import com.example.stock_monitor.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPriceHistoryRepository
        extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory>
    findBySymbolIgnoreCaseOrderByRecordedAtAsc(String symbol);
}