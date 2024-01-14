package me.dingtou.model;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易配置
 *
 * @author yuanhongbo
 */
public class TradeCfg {
    /**
     * 交易策略 价值平均、价格平均等
     */
    private String tradeStrategy;

    /**
     * 定投增量 500.00元
     */
    private BigDecimal increment;

    /**
     * 购买费率（百分比）
     */
    private BigDecimal serviceFeeRate;

    /**
     * 最低购买费用
     */
    private BigDecimal minServiceFee;

    /**
     * 最小交易份额
     */
    private BigDecimal minTradeAmount;

    /**
     * 可扩展属性
     */
    private Map<String, String> attributes;

    public static TradeCfg of(String tradeCfg) {
        return JSON.parseObject(tradeCfg, TradeCfg.class);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getTradeStrategy() {
        return tradeStrategy;
    }

    public void setTradeStrategy(String tradeStrategy) {
        this.tradeStrategy = tradeStrategy;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    public BigDecimal getServiceFeeRate() {
        return serviceFeeRate;
    }

    public void setServiceFeeRate(BigDecimal serviceFeeRate) {
        this.serviceFeeRate = serviceFeeRate;
    }

    public BigDecimal getMinServiceFee() {
        return minServiceFee;
    }

    public void setMinServiceFee(BigDecimal minServiceFee) {
        this.minServiceFee = minServiceFee;
    }

    public BigDecimal getMinTradeAmount() {
        return minTradeAmount;
    }

    public void setMinTradeAmount(BigDecimal minTradeAmount) {
        this.minTradeAmount = minTradeAmount;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
