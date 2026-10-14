package com.example.stock_monitor.service;

public class AlphaVantageRateLimitException
        extends RuntimeException {

    public AlphaVantageRateLimitException(String message) {
        super(message);
    }
}