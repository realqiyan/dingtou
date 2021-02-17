package me.dingtou.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockOrder {
    private Long id;
    private Long stockId;
    private String code;
    private Date createTime;
    private String type;
    private String outId;
    private Date tradeTime;
    private BigDecimal tradeFee;
    private BigDecimal tradeAmount;
    private BigDecimal tradeServiceFee;
    private String tradeStatus;
}
