package me.dingtou.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.quartz.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TradeManager {

    @Resource
    private StockOrderDAO stockOrderDAO;

    @Resource
    private StockManager stockManager;

    @Resource
    private PriceManager priceManager;

    @Resource
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
        for (TradeStrategy tradeStrategy : tradeStrategies) {
            if (tradeStrategy.isMatch(stock)) {
                TradeDetail calculate = tradeStrategy.calculateConform(stock, now);
                tradeFee = calculate.getTradeFee();
                tradeAmount = calculate.getTradeAmount();
            }
        }
        if (null == tradeFee) {
            tradeFee = stock.getTradeCfg().getIncrement();
        }
        order.setTradeFee(tradeFee);
        order.setTradeAmount(tradeAmount);
        // 服务费
        order.setTradeServiceFee(BigDecimal.ZERO);

        if (tradeFee.doubleValue() >= 0) {
            order.setType(TradeType.BUY);
            order.setOutId(buildOutId(TradeType.BUY, now, stock));
        } else {
            order.setType(TradeType.SELL);
            order.setOutId(buildOutId(TradeType.SELL, now, stock));
        }

        order.setTradeTime(buildTradeTime(now, stock));

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
        List<StockOrder> stockOrders = stockOrderDAO.selectList(query);
        return stockOrders.stream()
                .map(e -> OrderConvert.convert(stock, e))
                .collect(Collectors.toList());
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
        stockOrder.setTradeTime(order.getTradeTime());
        stockOrder.setTradeAmount(order.getTradeAmount());
        stockOrder.setTradeFee(order.getTradeFee());
        stockOrder.setTradeServiceFee(order.getTradeServiceFee());
        stockOrder.setTradeStatus(order.getStatus().getCode());
        stockOrder.setType(order.getType().getCode());
        stockOrderDAO.insert(stockOrder);
        stockManager.update(stock);
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

    private Date buildTradeTime(Date now, Stock stock) {
        if (StockType.FUND.equals(stock.getType())) {
            CronExpression cron = null;
            try {
                cron = new CronExpression(stock.getTradeCfg().getTradeCron());
                return cron.getNextValidTimeAfter(now);
            } catch (ParseException e) {
                return new Date();
            }
        } else {
            return now;
        }
    }

    private String buildOutId(TradeType type, Date now, Stock stock) {

        Date tradeDate = null;
        CronExpression cron = null;
        try {
            cron = new CronExpression(stock.getTradeCfg().getTradeCron());
            tradeDate = cron.getNextValidTimeAfter(now);
        } catch (ParseException e) {
            tradeDate = new Date();
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //buy_default_fund_000001_20210217
        return String.format("%s_%s_%s_%s_%s",
                stock.getOwner(),
                type.getCode(),
                stock.getMarket().getCode(),
                stock.getCode(),
                sdf.format(tradeDate));
    }

}
