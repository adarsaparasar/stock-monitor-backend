package com.example.stock_monitor.service;

import com.example.stock_monitor.model.Stock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public StockWebSocketService(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;

    }

    public void sendStockUpdate(Stock stock) {

        messagingTemplate.convertAndSend(
                "/topic/stocks",
                stock
        );
    }
}