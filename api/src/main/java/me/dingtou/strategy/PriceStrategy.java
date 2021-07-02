package me.dingtou.strategy;

import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 交易策略接口
 *
 * @author yuanhongbo
 */
public interface PriceStrategy {
    /**
     * 策略是否生效
     *
     * @param stock
     * @return
     */
    boolean isMatch(Stock stock);

    /**
     * 获取当前金额
     *
     * @param stock
     * @return
     */
    BigDecimal currentPrice(Stock stock);


    /**
     * 价格列表
     *
     * @param stock 股票基金
     * @param date  当前日期
     * @param x     交易日数量
     * @return
     */
    List<StockPrice> listPrice(Stock stock, Date date, int x);

    /**
     * 获取结算金额
     *
     * @param stock 标的
     * @param date  交易时间
     * @return
     */
    BigDecimal getSettlementPrice(Stock stock, Date date);
}
