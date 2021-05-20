package me.dingtou.dataobject;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Stock implements Serializable {
    private static final long serialVersionUID = 1527468059177627322L;
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
    private String marks;
}
