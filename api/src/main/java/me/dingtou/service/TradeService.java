package me.dingtou.service;

import me.dingtou.constant.StockType;
import me.dingtou.model.Order;

import java.util.List;

/**
 * 交易服务
 */
public interface TradeService {
    /**
     * 计算股票基金购买金额
     *
     * @param owner 所有者
     * @param type 股票类型
     * @param code 股票代码
     * @return 订单对象
     */
    Order conform(String owner, StockType type, String code);

    /**
     * 购买股票基金
     *
     * @param order 订单对象
     * @return 订单对象
     */
    Order buy(Order order);

    /**
     * 调整订单
     *
     * @param order 订单对象
     * @return 订单对象
     */
    Order adjust(Order order);

    /**
     * 交易结算
     *
     * @param owner 所有者
     * @return 结算后的订单列表
     */
    List<Order> settlement(String owner);

    /**
     * 重新统计交易数据
     *
     * @param owner 所有者
     * @return 是否成功重新统计交易数据
     */
    boolean statistic(String owner);

    /**
     * 自动生成调整单
     *
     * @param owner 所有者
     * @return 自动生成的调整单列表
     */
    List<Order> autoAdjust(String owner);

}
