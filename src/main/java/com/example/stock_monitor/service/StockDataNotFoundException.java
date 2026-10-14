
package com.example.stock_monitor.service;

public class StockDataNotFoundException extends RuntimeException {

    public StockDataNotFoundException(String message) {
        super(message);
    }
}
