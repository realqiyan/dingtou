package me.dingtou.manager;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.OrderSnapshotKeys;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.constant.TradeType;
import me.dingtou.dao.StockOrderDAO;
import me.dingtou.dataobject.StockOrder;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeDetail;
import me.dingtou.strategy.TradeStrategy;
import me.dingtou.util.OrderConvert;
import me.dingtou.util.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TradeManager {

    @Autowired
    private StockOrderDAO stockOrderDAO;

    @Autowired
    private StockManager stockManager;

    @Autowired
    private List<TradeStrategy> tradeStrategies;

    /**
     * 计算订单金额
     *
     * @param stock
     * @return
     */
    public Order conform(Stock stock) {
        Order order = new Order();
        order.setStock(stock);
        order.setStatus(TradeStatus.PROCESSING);
        Date now = new Date();
        order.setCreateTime(now);

        BigDecimal tradeFee = null;
        BigDecimal tradeAmount = null;
        BigDecimal tradeServiceFee = null;
        // 所有订单拿出来实时统计
        List<Order> stockOrders = getStockOrder(stock.getOwner(), stock.getType(), stock.getCode());
        TradeDetail calculate = null;
        for (TradeStrategy tradeStrategy : tradeStrategies) {
            if (tradeStrategy.isMatch(stock)) {
                calculate = tradeStrategy.calculateConform(stock, stockOrders, now);
                tradeFee = calculate.getTradeFee();
                tradeAmount = calculate.getTradeAmount();
                tradeServiceFee = calculate.getTradeServiceFee();
            }
        }
        if (null == tradeFee) {
            tradeFee = stock.getTradeCfg().getIncrement();
        }
        if (null == tradeFee) {
            tradeFee = BigDecimal.ZERO;
        }
        if (null == tradeAmount) {
            tradeAmount = BigDecimal.ZERO;
        }
        if (null == tradeServiceFee) {
            tradeServiceFee = BigDecimal.ZERO;
        }
        order.setTradeFee(tradeFee);
        order.setTradeAmount(tradeAmount);
        // 服务费
        order.setTradeServiceFee(tradeServiceFee);

        if (tradeFee.doubleValue() >= 0) {
            order.setType(TradeType.BUY);
            order.setOutId(buildOutId(TradeType.BUY, now, stock));
        } else {
            order.setType(TradeType.SELL);
            order.setOutId(buildOutId(TradeType.SELL, now, stock));
        }

        order.setTradeTime(OrderUtils.getTradeDate(now));

        // 交易快照
        Map<String, String> snapshot = new HashMap<>();
        snapshot.put(OrderSnapshotKeys.TRADE_CFG, JSON.toJSONString(stock.getTradeCfg()));
        if (null != calculate && null != calculate.getSellOrders()) {
            List<String> outIds = calculate.getSellOrders().stream().map(Order::getOutId).collect(Collectors.toList());
            snapshot.put(OrderSnapshotKeys.BUY_ORDER_OUT_IDS, JSON.toJSONString(outIds));
            order.setDependencies(calculate.getSellOrders());
        }
        order.setSnapshot(snapshot);

        return order;
    }

    /**
     * 查询交易订单
     *
     * @param owner
     * @param type
     * @param code
     * @return
     */
    public List<Order> getStockOrder(String owner, StockType type, String code) {
        Stock stock = stockManager.query(owner, type, code);
        QueryWrapper<StockOrder> query = new QueryWrapper<StockOrder>();
        query.eq("stock_id", stock.getId());
        query.orderByDesc("create_time");
        List<StockOrder> stockOrders = stockOrderDAO.selectList(query);
        return stockOrders.stream().map(e -> OrderConvert.convert(stock, e)).collect(Collectors.toList());
    }

    public void importOrder(Order order) {
        Order dbOrder = queryByOutId(order.getStock(), order.getOutId());
        if (null != dbOrder) {
            return;
        }
        StockOrder stockOrder = new StockOrder();
        Stock stock = order.getStock();
        stockOrder.setStockId(stock.getId());
        stockOrder.setCode(stock.getCode());
        stockOrder.setOutId(order.getOutId());
        stockOrder.setCreateTime(order.getCreateTime());
        stockOrder.setTradeTime(order.getTradeTime());
        stockOrder.setTradeAmount(order.getTradeAmount());
        stockOrder.setTradeFee(order.getTradeFee());
        stockOrder.setTradeServiceFee(order.getTradeServiceFee());
        stockOrder.setTradeStatus(order.getStatus().getCode());
        stockOrder.setType(order.getType().getCode());
        stockOrderDAO.insert(stockOrder);
    }

    /**
     * 下单
     *
     * @param order
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Order buy(Order order) {
        Order dbOrder = queryByOutId(order.getStock(), order.getOutId());
        if (null != dbOrder) {
            return dbOrder;
        }
        StockOrder stockOrder = new StockOrder();
        Stock stock = order.getStock();
        stockOrder.setStockId(stock.getId());
        stockOrder.setCode(stock.getCode());
        stockOrder.setOutId(order.getOutId());
        stockOrder.setCreateTime(order.getCreateTime());
        Date tradeTime = OrderUtils.getTradeDate(order.getCreateTime());
        stockOrder.setTradeTime(tradeTime);
        stockOrder.setTradeAmount(order.getTradeAmount());
        stockOrder.setTradeFee(order.getTradeFee());
        stockOrder.setTradeServiceFee(order.getTradeServiceFee());
        stockOrder.setTradeStatus(order.getStatus().getCode());
        stockOrder.setType(order.getType().getCode());
        stockOrder.setSnapshot(JSON.toJSONString(order.getSnapshot()));
        stockOrderDAO.insert(stockOrder);
        stockManager.update(stock);
        return queryByOutId(order.getStock(), order.getOutId());
    }

    /**
     * 调整
     *
     * @param order
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Order adjust(Order order, boolean withStock) {
        StockOrder stockOrder = new StockOrder();
        Stock stock = order.getStock();
        stockOrder.setStockId(stock.getId());
        stockOrder.setCode(stock.getCode());
        stockOrder.setOutId(order.getOutId());
        stockOrder.setCreateTime(order.getCreateTime());
        stockOrder.setTradeTime(order.getTradeTime());
        stockOrder.setTradeAmount(order.getTradeAmount());
        stockOrder.setTradeFee(order.getTradeFee());
        stockOrder.setTradeServiceFee(order.getTradeServiceFee());
        stockOrder.setTradeStatus(order.getStatus().getCode());
        stockOrder.setType(order.getType().getCode());
        stockOrderDAO.insert(stockOrder);
        if (withStock) {
            stock.setAmount(stock.getAmount().add(order.getTradeAmount()));
            stock.setTotalFee(stock.getTotalFee().add(order.getTradeFee()));
            stockManager.update(stock);
        }
        return queryByOutId(order.getStock(), order.getOutId());
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Order> settlement(Stock stock) {
        List<Order> orders = new ArrayList<>();

        QueryWrapper<StockOrder> query = new QueryWrapper<StockOrder>();
        query.eq("stock_id", stock.getId());
        query.eq("trade_status", TradeStatus.PROCESSING.getCode());
        query.orderByAsc("trade_time");
        List<StockOrder> stockOrders = stockOrderDAO.selectList(query);

        BigDecimal amount = stock.getAmount();
        BigDecimal tradeFee = stock.getTotalFee();
        Date lastTradeTime = null;
        for (StockOrder stockOrder : stockOrders) {
            lastTradeTime = stockOrder.getTradeTime();
            for (TradeStrategy tradeStrategy : tradeStrategies) {
                if (tradeStrategy.isMatch(stock)) {
                    TradeDetail calculate = tradeStrategy.calculateSettlement(OrderConvert.convert(stock, stockOrder));
                    if (null == calculate) {
                        // 价格没有更新时回滚整个交易结算
                        log.error("TradeDetail is null，stockCode:" + stockOrder.getCode());
                        throw new RuntimeException("TradeDetail is null");
                    }
                    stockOrder.setTradeStatus(TradeStatus.DONE.getCode());
                    stockOrder.setTradeFee(calculate.getTradeFee());
                    stockOrder.setTradeAmount(calculate.getTradeAmount());
                    stockOrder.setTradeServiceFee(calculate.getTradeServiceFee());
                    stockOrderDAO.updateById(stockOrder);
                    amount = amount.add(stockOrder.getTradeAmount());
                    tradeFee = tradeFee.add(stockOrder.getTradeFee());
                }
            }
            orders.add(OrderConvert.convert(stock, stockOrder));
        }
        stock.setAmount(amount);
        stock.setTotalFee(tradeFee);
        stock.setTradeStatus(TradeStatus.DONE);
        stock.setLastTradeTime(lastTradeTime);
        stockManager.update(stock);

        return orders;

    }


    /**
     * 根据外部ID获取订单
     *
     * @param stock
     * @param outId
     * @return
     */
    public Order queryByOutId(Stock stock, String outId) {
        QueryWrapper<StockOrder> query = new QueryWrapper<StockOrder>();
        query.eq("out_id", outId);
        List<StockOrder> stockOrders = stockOrderDAO.selectList(query);
        if (null != stockOrders && !stockOrders.isEmpty()) {
            return OrderConvert.convert(stock, stockOrders.get(0));
        }
        return null;
    }

    private String buildOutId(TradeType type, Date now, Stock stock) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //buy_default_fund_000001_20210217
        return String.format("%s_%s_%s_%s_%s", stock.getOwner(), type.getCode(), stock.getMarket().getCode(), stock.getCode(), sdf.format(now));
    }

}
