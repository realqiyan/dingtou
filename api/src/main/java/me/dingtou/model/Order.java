package me.dingtou.model;

import me.dingtou.constant.TradeStatus;
import me.dingtou.constant.TradeType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 交易订单
 *
 * @author yuanhongbo
 */
public class Order {
    /**
     * 订单号
     */
    private Long orderId;

    /**
     * 外部唯一ID
     */
    private String outId;
    /**
     * 证券
     */
    private Stock stock;
    /**
     * 交易类型
     */
    private TradeType type;
    /**
     * 交易下单时间
     */
    private Date createTime;
    /**
     * 交易交割时间
     */
    private Date tradeTime;
    /**
     * 交易金额
     */
    private BigDecimal tradeFee;
    /**
     * 交易份额
     */
    private BigDecimal tradeAmount;
    /**
     * 交易服务费
     */
    private BigDecimal tradeServiceFee;
    /**
     * 交易状态
     */
    private TradeStatus status;
    /**
     * 当前盈亏金额
     */
    private BigDecimal currentProfitFee;
    /**
     * 当前盈亏比例
     */
    private BigDecimal currentProfitRatio;

    /**
     * 交易快照
     */
    private Map<String, String> snapshot;


    /**
     * 依赖订单列表
     */
    private List<Order> dependencies;

    public Long getOrderId() {
        return orderId;
    }

    public String getStockCode() {
        if (null == this.getStock()) {
            return null;
        }
        return this.getStock().getCode();
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public TradeType getType() {
        return type;
    }

    public void setType(TradeType type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
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

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public BigDecimal getCurrentProfitFee() {
        return currentProfitFee;
    }

    public void setCurrentProfitFee(BigDecimal currentProfitFee) {
        this.currentProfitFee = currentProfitFee;
    }

    public BigDecimal getCurrentProfitRatio() {
        return currentProfitRatio;
    }

    public void setCurrentProfitRatio(BigDecimal currentProfitRatio) {
        this.currentProfitRatio = currentProfitRatio;
    }

    public Map<String, String> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, String> snapshot) {
        this.snapshot = snapshot;
    }

    public List<Order> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Order> dependencies) {
        this.dependencies = dependencies;
    }
}
