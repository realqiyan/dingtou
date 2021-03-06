package me.dingtou.service;

import me.dingtou.constant.StockType;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;

import java.util.List;

/**
 * 交易服务
 */
public interface TradeService {

    /**
     * 计算股票基金购买金额
     *
     * @param owner
     * @param type
     * @param code
     * @return
     */
    Order conform(String owner, StockType type, String code);

    /**
     * 购买股票基金
     *
     * @param order
     * @return
     */
    Order buy(Order order);


    /**
     * 调整订单
     *
     * @param order
     * @return
     */
    Order adjust(Order order);

    /**
     * 交易结算
     *
     * @param owner
     * @return
     */
    List<Order> settlement(String owner);

    /**
     * 重新统计交易数据
     *
     * @param owner
     * @return
     */
    boolean statistic(String owner);
}
