package me.dingtou.strategy;

import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeDetail;

import java.util.Date;

/**
 * 交易策略接口
 *
 * @author yuanhongbo
 */
public interface TradeStrategy {
    /**
     * 策略是否生效
     *
     * @param stock
     * @return
     */
    boolean isMatch(Stock stock);

    /**
     * 计算下单金额&份额
     *
     * @param stock
     * @param date
     * @return
     */
    TradeDetail calculateConform(Stock stock, Date date);

    /**
     * 结算实际下单金额&份额
     *
     * @param order
     * @return
     */
    TradeDetail calculateSettlement(Order order);

}
