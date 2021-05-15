package me.dingtou.model;

import java.util.List;

/**
 * 报表视图
 *
 * @author yuanhongbo
 */
public class ChartView {
    /**
     * name
     */
    private String name;
    /**
     * value
     */
    private String value;
    /**
     * children
     */
    private List<ChartView> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ChartView> getChildren() {
        return children;
    }

    public void setChildren(List<ChartView> children) {
        this.children = children;
    }
}
