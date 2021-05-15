package me.dingtou.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Stock {
    private Long id;
    private String name;
    private String code;
    private String type;
    private String market;
    private String owner;
    private String tradeCfg;
    private BigDecimal totalFee;
    private BigDecimal amount;
    private Date lastTradeTime;
    private String tradeStatus;
    private String category;
    private String subCategory;
    private Integer status;
}
