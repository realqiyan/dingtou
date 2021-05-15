package me.dingtou.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 资产分类
 *
 * @author yuanhongbo
 */
public class CategoryAsset {
    /**
     * 分类
     */
    private String category;
    /**
     * 子分类
     */
    private String subCategory;
    /**
     * 当前金额（元）
     */
    private BigDecimal totalFee;
    /**
     * 占比
     */
    private BigDecimal ratio;
    /**
     * 资产列表
     */
    private List<Asset> assetList;

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

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public List<Asset> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryAsset that = (CategoryAsset) o;
        return Objects.equals(category, that.category) &&
                Objects.equals(subCategory, that.subCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, subCategory);
    }
}
