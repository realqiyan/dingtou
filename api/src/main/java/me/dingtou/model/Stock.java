package me.dingtou.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.dingtou.constant.Market;
import me.dingtou.constant.Status;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票基金配置
 *
 * @author yuanhongbo
 */
public class Stock {
    /**
     * ID
     */
    private Long id;
    /**
     * 股票基金编码
     */
    private String code;
    /**
     * 股票基金类型
     */
    private StockType type;
    /**
     * 市场
     */
    private Market market;
    /**
     * 持有人
     */
    private String owner;
    /**
     * 交易配置
     */
    private TradeCfg tradeCfg;
    /**
     * 总投入金额（元）
     */
    private BigDecimal totalFee;
    /**
     * 总持有份数
     */
    private BigDecimal amount;
    /**
     * 最后交易时间
     */
    private Date lastTradeTime;
    /**
     * 交易状态
     */
    private TradeStatus tradeStatus;
    /**
     * 状态
     */
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public StockType getType() {
        return type;
    }

    public void setType(StockType type) {
        this.type = type;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public TradeCfg getTradeCfg() {
        return tradeCfg;
    }

    public void setTradeCfg(TradeCfg tradeCfg) {
        this.tradeCfg = tradeCfg;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getLastTradeTime() {
        return lastTradeTime;
    }

    public void setLastTradeTime(Date lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
