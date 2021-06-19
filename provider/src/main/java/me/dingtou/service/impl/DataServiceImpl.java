package me.dingtou.service.impl;

import me.dingtou.manager.StockManager;
import me.dingtou.manager.TradeManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPackage;
import me.dingtou.service.DataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            stockOrder.stream().forEach(e -> {
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
    @Transactional(rollbackFor = Exception.class)
    public boolean importData(String owner, StockPackage data) {
        if (null == data || (null == data.getStocks() && null == data.getOrders())) {
            return false;
        }

        //更新owner
        if (null != data.getStocks()) {
            for (Stock stock : data.getStocks()) {
                if (null == stock) {
                    continue;
                }
                stock.setOwner(owner);
            }
        }

        List<Stock> stocks = data.getStocks();
        // oldId -> newStock
        Map<Long, Stock> idMapping = new HashMap<>(stocks.size(), 1.0f);
        if (null != stocks) {
            for (Stock stock : stocks) {
                Long oldId = stock.getId();
                Stock dbStock = stockManager.query(stock.getOwner(), stock.getType(), stock.getCode());
                if (null != dbStock) {
                    idMapping.put(oldId, dbStock);
                    continue;
                }
                // 重置ID
                stock.setId(null);
                Stock newStock = stockManager.create(stock);
                if (null != newStock) {
                    idMapping.put(oldId, newStock);
                }
            }
        }

        List<Order> orders = data.getOrders();
        if (null != orders) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            for (Order order : orders) {
                if (null == order || null == order.getStock()) {
                    continue;
                }
                Long oldId = order.getStock().getId();
                Stock newStock = idMapping.get(oldId);
                if (null == newStock) {
                    continue;
                }
                order.setStock(newStock);
                Stock stock = order.getStock();
                // 更新outId
                String newOutId = String.format("%s_%s_%s_%s_%s",
                        stock.getOwner(),
                        order.getType().getCode(),
                        stock.getMarket().getCode(),
                        stock.getCode(),
                        sdf.format(order.getTradeTime()));
                order.setOutId(newOutId);
                tradeManager.importOrder(order);
            }
        }
        return true;
    }
}
