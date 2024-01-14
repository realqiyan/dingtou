package me.dingtou.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易详情（包含：买入金额和份数）
 */
public class TradeDetail {
    /**
     * 目标金额
     */
    private BigDecimal targetValue;
    /**
     * 交易金额
     */
    private BigDecimal tradeFee;
    /**
     * 交易份额
     */
    private BigDecimal tradeAmount;
    /**
     * 交易手续费
     */
    private BigDecimal tradeServiceFee;

    /**
     * 卖出的订单
     */
    private List<Order> sellOrders;

    public TradeDetail() {
    }

    public TradeDetail(BigDecimal targetValue, BigDecimal tradeFee, BigDecimal tradeAmount, BigDecimal tradeServiceFee) {
        this.targetValue = targetValue;
        this.tradeFee = tradeFee;
        this.tradeAmount = tradeAmount;
        this.tradeServiceFee = tradeServiceFee;

    }

    public TradeDetail(BigDecimal targetValue, BigDecimal tradeFee, BigDecimal tradeAmount, BigDecimal tradeServiceFee, List<Order> sellOrders) {
        this(targetValue, tradeFee, tradeAmount, tradeServiceFee);
        this.sellOrders = sellOrders;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(BigDecimal targetValue) {
        this.targetValue = targetValue;
    }

    public BigDecimal getTradeFee() {
        return tradeFee;
    }

    public void setTradeFee(BigDecimal tradeFee) {
        this.tradeFee = tradeFee;
    }

    public BigDecimal getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(BigDecimal tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public BigDecimal getTradeServiceFee() {
        return tradeServiceFee;
    }

    public void setTradeServiceFee(BigDecimal tradeServiceFee) {
        this.tradeServiceFee = tradeServiceFee;
    }

    public List<Order> getSellOrders() {
        return sellOrders;
    }
}
