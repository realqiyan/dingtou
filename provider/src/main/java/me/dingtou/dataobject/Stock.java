package me.dingtou.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Stock {
    /**
     * ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 代码
     */
    private String code;
    /**
     * 类型
     */
    private String type;
    /**
     * 市场
     */
    private String market;
    /**
     * 拥有者
     */
    private String owner;
    /**
     * 交易配置
     */
    private String tradeCfg;
    /**
     * 总费用
     */
    private BigDecimal totalFee;
    /**
     * 总份额
     */
    private BigDecimal amount;
    /**
     * 最后交易时间
     */
    private Date lastTradeTime;
    /**
     * 交易状态
     */
    private String tradeStatus;
    /**
     * 类别
     */
    private String category;
    /**
     * 子类别
     */
    private String subCategory;
    /**
     * 状态
     */
    private Integer status;
}

