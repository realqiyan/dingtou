package me.dingtou.model;

import lombok.Data;
import me.dingtou.constant.Market;
import me.dingtou.constant.Status;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票基金配置
 *
 * @author yuanhongbo
 */
@Data
public class Stock implements Serializable {
    private static final long serialVersionUID = 3743571947682876315L;
    /**
     * ID
     */
    private Long id;
    /**
     * 股票基金编码
     */
    private String code;
    /**
     * 股票基金名字
     */
    private String name;
    /**
     * 当前价格
     */
    private BigDecimal currentPrice;
    /**
     * 当前价值
     */
    private BigDecimal currentValue;
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
     * 分类
     */
    private String category;
    /**
     * 子分类
     */
    private String subCategory;
    /**
     * 状态
     */
    private Status status;
    /**
     * 标注
     */
    private String marks;

    public String getStockUniqueKey() {
        return String.format("%s_%s_%s", this.type, this.market, this.code);
    }
}
