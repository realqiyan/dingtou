package me.dingtou.service.impl;

import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.constant.TradeType;
import me.dingtou.manager.StockManager;
import me.dingtou.manager.TradeManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.service.TradeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
public class TradeServiceImpl implements TradeService {

    @Resource
    private StockManager stockManager;


    @Resource
    private TradeManager tradeManager;

    @Override
    public Order conform(String owner, StockType type, String code) {
        Stock stock = stockManager.query(owner, type, code);
        if (null == stock) {
            return null;
        }
        return tradeManager.conform(stock);
    }

    @Override
    public Order buy(Order order) {
        return tradeManager.buy(order);
    }

    @Override
    public Order adjust(Order order) {
        return tradeManager.adjust(order);
    }

    @Override
    public List<Order> settlement(String owner) {
        List<Order> result = new ArrayList<>();
        // 查询所有标的
        List<Stock> stocks = stockManager.query(owner, null);
        // 找出未完成的交易
        for (Stock stock : stocks) {
            List<Order> settlement = tradeManager.settlement(stock);
            if (null == settlement) {
                continue;
            }
            result.addAll(settlement);
        }
        return result;
    }

    @Override
    public boolean statistic(String owner) {

        // 查询所有标的
        List<Stock> stocks = stockManager.query(owner, null);
        // 找出未完成的交易
        for (Stock stock : stocks) {
            List<Order> orders = tradeManager.getStockOrder(stock.getOwner(), stock.getType(), stock.getCode());
            if (null == orders) {
                continue;
            }
            BigDecimal totalFee = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Order order : orders) {
                if (!TradeStatus.DONE.equals(order.getStatus())) {
                    continue;
                }
                totalFee = totalFee.add(order.getTradeFee());
                totalAmount = totalAmount.add(order.getTradeAmount());
            }
            stock.setTotalFee(totalFee);
            stock.setAmount(totalAmount);
            stockManager.update(stock);
        }
        return true;
    }

    @Override
    public List<Order> autoAdjust(String owner) {
        List<Stock> stocks = stockManager.query(owner, null);
        if (null == stocks || stocks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Order> adjustOrders = new ArrayList<>();
        for (Stock stock : stocks) {
            List<Order> stockOrder = tradeManager.getStockOrder(owner, stock.getType(), stock.getCode());
            BigDecimal totalOrderTradeFee = BigDecimal.ZERO;
            BigDecimal totalOrderTradeAmount = BigDecimal.ZERO;
            if (null != stockOrder && !stockOrder.isEmpty()) {
                for (Order order : stockOrder) {
                    totalOrderTradeFee = totalOrderTradeFee.add(order.getTradeFee());
                    totalOrderTradeAmount = totalOrderTradeAmount.add(order.getTradeAmount());
                }
            }
            BigDecimal totalFee = stock.getTotalFee();
            BigDecimal totalAmount = stock.getAmount();

            BigDecimal adjustFee = totalFee.subtract(totalOrderTradeFee);
            BigDecimal adjustAmount = totalAmount.subtract(totalOrderTradeAmount);
            if (adjustFee.doubleValue() == 0.0 && adjustAmount.doubleValue() == 0.0) {
                continue;
            }

            Order order = new Order();
            order.setStock(stock);
            order.setType(TradeType.ADJUST);
            order.setStatus(TradeStatus.DONE);
            Date now = new Date();
            order.setTradeTime(now);
            order.setCreateTime(now);
            order.setOutId(String.valueOf(System.currentTimeMillis()));
            order.setTradeFee(adjustFee);
            order.setTradeAmount(adjustAmount);
            order.setTradeServiceFee(BigDecimal.ZERO);
            adjustOrders.add(this.adjust(order));
        }

        return adjustOrders;
    }
}
