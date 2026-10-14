package com.example.stock_monitor.service;

import com.example.stock_monitor.dto.StockRequest;
import com.example.stock_monitor.model.Stock;
import com.example.stock_monitor.repository.StockRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // Cache all stocks
    @Cacheable(value = "stocks", key = "'all'")
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    // Cache a stock by symbol
    @Cacheable(
            value = "stocks",
            key = "'symbol:' + #symbol.toUpperCase()"
    )
    public Stock getStock(String symbol) {
        return stockRepository.findBySymbolIgnoreCase(symbol)
                .orElse(null);
    }

    // Add a stock from an entity
    @CacheEvict(value = "stocks", allEntries = true)
    public Stock addStock(Stock stock) {
        return stockRepository.save(stock);
    }

    // Update an existing stock
    @CacheEvict(value = "stocks", allEntries = true)
    public Stock updateStock(Long id, Stock updatedStock) {
        return stockRepository.findById(id)
                .map(existingStock -> {
                    existingStock.setSymbol(updatedStock.getSymbol());
                    existingStock.setCompanyName(
                            updatedStock.getCompanyName());
                    existingStock.setPrice(updatedStock.getPrice());
                    existingStock.setChange(updatedStock.getChange());
                    existingStock.setChangePercent(
                            updatedStock.getChangePercent());

                    return stockRepository.save(existingStock);
                })
                .orElse(null);
    }

    // Delete a stock
    @CacheEvict(value = "stocks", allEntries = true)
    public boolean deleteStock(Long id) {
        if (stockRepository.existsById(id)) {
            stockRepository.deleteById(id);
            return true;
        }

        return false;
    }

    // Add a stock from a request DTO
    @CacheEvict(value = "stocks", allEntries = true)
    public Stock addStock(StockRequest request) {
        Stock stock = new Stock(
                request.getSymbol(),
                request.getCompanyName(),
                request.getPrice(),
                request.getChangePercent()
        );

        return stockRepository.save(stock);
    }
}
