package me.dingtou.strategy;

import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeDetail;

import java.util.Date;
import java.util.List;

/**
 * 交易策略接口
 *
 * @author yuanhongbo
 */
public interface TradeStrategy {
    /**
     * 策略是否生效
     *
     * @param stock 股票对象
     * @return 是否生效
     */
    boolean isMatch(Stock stock);

    /**
     * 计算下单金额&份额
     *
     * @param stock 股票对象
     * @param stockOrders 订单列表
     * @param tradeTime 交易时间
     * @return 交易详情对象
     */
    TradeDetail calculateConform(Stock stock, List<Order> stockOrders, Date tradeTime);

    /**
     * 结算实际下单金额&份额
     *
     * @param order 订单对象
     * @return 交易详情对象
     */
    TradeDetail calculateSettlement(Order order);


}
