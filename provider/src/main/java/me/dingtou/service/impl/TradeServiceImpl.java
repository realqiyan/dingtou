package me.dingtou.service.impl;

import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.manager.StockManager;
import me.dingtou.manager.TradeManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.service.TradeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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
}
