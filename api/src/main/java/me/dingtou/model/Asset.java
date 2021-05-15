package me.dingtou.model;


import java.math.BigDecimal;

/**
 * 股票基金配置
 *
 * @author yuanhongbo
 */
public class Asset {

    /**
     * 资产编码
     */
    private String code;
    /**
     * 资产名字
     */
    private String name;
    /**
     * 当前价格（元）
     */
    private BigDecimal currentPrice;
    /**
     * 当前金额（元）
     */
    private BigDecimal totalFee;
    /**
     * 总持有份数
     */
    private BigDecimal amount;
    /**
     * 分类
     */
    private String category;
    /**
     * 子分类
     */
    private String subCategory;
    /**
     * 占比
     */
    private BigDecimal ratio;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

}
