package me.dingtou.service;

import java.util.List;

import me.dingtou.constant.StockType;
import me.dingtou.model.Order;
import me.dingtou.model.StockOrder;
import me.dingtou.result.AppPageResult;

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
     * 查询订单
     * @param owner
     * @param stockId
     * @param current
     * @param pageSize
     * @return
     */
    AppPageResult<StockOrder> queryStockOrder(String owner, Long stockId, int current, int pageSize);

    /**
     * 更新订单
     * @param owner
     * @param stockOrder
     * @return
     */
    int updateStockOrder(String owner, StockOrder stockOrder);

    /**
     * 删除交易订单
     * @param owner
     * @param id
     * @return
     */
    int deleteStockOrder(String owner, Long id);

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
