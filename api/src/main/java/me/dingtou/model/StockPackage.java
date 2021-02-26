package me.dingtou.model;

import java.util.List;

public class StockPackage {
    private List<Stock> stocks;
    private List<Order> orders;

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
