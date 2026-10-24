
package com.example.stock_monitor.controller;

import com.example.stock_monitor.service.StockPriceScheduler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks/test")
public class StockPriceTestController {

    private final StockPriceScheduler scheduler;

    public StockPriceTestController(StockPriceScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/update")
    public String updatePriceNow() {
        scheduler.updateIbmPriceNow();
        return "IBM price fetched and history saved successfully";
    }
}
