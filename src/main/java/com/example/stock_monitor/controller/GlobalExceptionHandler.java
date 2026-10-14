
        package com.example.stock_monitor.controller;

import com.example.stock_monitor.service.AlphaVantageRateLimitException;
import com.example.stock_monitor.service.StockDataNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle missing stock data (HTTP 404)
    @ExceptionHandler(StockDataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleStockNotFound(
            StockDataNotFoundException exception) {

        return Map.of(
                "status", 404,
                "message", exception.getMessage()
        );
    }

    // Handle Alpha Vantage API limits (HTTP 429)
    @ExceptionHandler(AlphaVantageRateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, Object> handleRateLimit(
            AlphaVantageRateLimitException exception) {

        return Map.of(
                "status", 429,
                "message", exception.getMessage()
        );
    }

    // Handle unexpected errors (HTTP 500)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnexpectedError(
            RuntimeException exception) {

        return Map.of(
                "status", 500,
                "message", "An unexpected error occurred."
        );
    }
}