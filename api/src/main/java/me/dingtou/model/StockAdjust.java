package me.dingtou.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票复权信息-前复权
 */
public class StockAdjust implements Comparable<StockAdjust> {
    /**
     * 复权代码
     */
    private String stockCode;
    /**
     * 复权日期
     */
    private Date adjustDate;
    /**
     * 复权比例
     */
    private BigDecimal adjustVal;

    public StockAdjust(String stockCode, Date adjustDate, BigDecimal adjustVal) {
        this.stockCode = stockCode;
        this.adjustDate = adjustDate;
        this.adjustVal = adjustVal;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public Date getAdjustDate() {
        return adjustDate;
    }

    public void setAdjustDate(Date adjustDate) {
        this.adjustDate = adjustDate;
    }


    public BigDecimal getAdjustVal() {
        return adjustVal;
    }

    public void setAdjustVal(BigDecimal adjustVal) {
        this.adjustVal = adjustVal;
    }

    @Override
    public int compareTo(StockAdjust o) {
        if (null == o || null == o.adjustDate) {
            return 0;
        }
        return o.adjustDate.compareTo(this.adjustDate);
    }
}
