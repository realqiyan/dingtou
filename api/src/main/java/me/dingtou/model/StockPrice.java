package me.dingtou.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票基金价格
 */
public class StockPrice {

    /**
     * 股票基金
     */
    private Stock stock;

    /**
     * 日期
     */
    private Date date;

    /**
     * 价格
     */
    private BigDecimal price;

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
