package me.dingtou.service.impl;

import me.dingtou.manager.StockManager;
import me.dingtou.manager.TradeManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPackage;
import me.dingtou.service.DataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Resource
    private StockManager stockManager;
    @Resource
    private TradeManager tradeManager;


    @Override
    public StockPackage exportData(String owner) {
        StockPackage stockPackage = new StockPackage();
        List<Stock> stocks = stockManager.query(owner, null);
        if (null == stocks || stocks.isEmpty()) {
            return stockPackage;
        }
        stockPackage.setStocks(stocks);

        List<Order> orders = new ArrayList<>();
        for (Stock stock : stocks) {
            List<Order> stockOrder = tradeManager.getStockOrder(stock.getOwner(), stock.getType(), stock.getCode());
            if (null == stockOrder || stockOrder.isEmpty()) {
                continue;
            }
            stockOrder.stream().forEach(e->{
                Stock orderStock = e.getStock();
                Stock dataStock = new Stock();
                dataStock.setId(orderStock.getId());
                dataStock.setCode(orderStock.getCode());
                e.setStock(dataStock);
            });
            orders.addAll(stockOrder);
        }
        stockPackage.setOrders(orders);
        return stockPackage;
    }

    @Override
    public boolean importData(StockPackage data) {
        if (null == data || (null == data.getStocks() && null == data.getOrders())) {
            return false;
        }

        List<Stock> stocks = data.getStocks();
        if (null != stocks) {
            for (Stock stock : stocks) {
                Stock dbStock = stockManager.query(stock.getOwner(), stock.getType(), stock.getCode());
                if (null != dbStock) {
                    continue;
                }
                stockManager.create(stock);
            }
        }

        List<Order> orders = data.getOrders();
        if (null != orders) {
            for (Order order : orders) {
                tradeManager.importOrder(order);
            }
        }
        return true;
    }
}
