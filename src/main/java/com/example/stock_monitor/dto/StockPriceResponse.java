package com.example.stock_monitor.dto;

public class StockPriceResponse {

    private String symbol;
    private double price;
    private double change;
    private String changePercent;

    public StockPriceResponse() {
    }

    public StockPriceResponse(
            String symbol,
            double price,
            double change,
            String changePercent) {

        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

}