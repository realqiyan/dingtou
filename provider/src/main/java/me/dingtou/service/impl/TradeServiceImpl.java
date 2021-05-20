package me.dingtou.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.manager.StockManager;
import me.dingtou.manager.TradeManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.StockOrder;
import me.dingtou.result.AppPageResult;
import me.dingtou.service.TradeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


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
    public int updateStockOrder(String owner, StockOrder stockOrder) {
        return tradeManager.updateStockOrder(JSON.parseObject(JSON.toJSONString(stockOrder), me.dingtou.dataobject.StockOrder.class));
    }

    @Override
    public int deleteStockOrder(String owner, Long id) {
        return tradeManager.deleteStockOrder(id);
    }

    @Override
    public AppPageResult<StockOrder> queryStockOrder(String owner, Long stockId, int current, int pageSize){
        Stock stock = stockManager.queryById(stockId);

        Integer count = Optional.ofNullable(stock)
            .map(x->tradeManager.selectOrderCountByStockId(x.getId()))
            .orElse(0);
        if(count<=0){
            return AppPageResult.success();
        }
        return Optional.ofNullable(stock)
            .filter(x-> StringUtils.equals(x.getOwner(), owner))
            .map(x->tradeManager.getStockOrder(stockId, current, pageSize))
            .map(x->{
                AppPageResult<StockOrder> result = AppPageResult.success(JSON.parseArray(JSON.toJSONString(x), StockOrder.class));
                result.setTotal(count);
                result.setCurrent(current);
                result.setPageSize(pageSize);
                return result; })
            .orElse(AppPageResult.success());
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
