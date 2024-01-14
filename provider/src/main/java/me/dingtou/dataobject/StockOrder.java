package me.dingtou.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockOrder {
    /**
     * 交易唯一ID
     */
    private Long id;
    /**
     * 股票唯一ID
     */
    private Long stockId;
    /**
     * 交易代码
     */
    private String code;
    /**
     * 交易创建时间
     */
    private Date createTime;
    /**
     * 交易类型
     */
    private String type;
    /**
     * 外部交易幂等标识符
     */
    private String outId;
    /**
     * 交易时间
     */
    private Date tradeTime;
    /**
     * 交易费用
     */
    private BigDecimal tradeFee;
    /**
     * 交易数量
     */
    private BigDecimal tradeAmount;
    /**
     * 交易服务费用
     */
    private BigDecimal tradeServiceFee;
    /**
     * 交易状态
     */
    private String tradeStatus;
    /**
     * 交易快照
     */
    private String snapshot;

}
