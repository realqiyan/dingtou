package me.dingtou.constant;

/**
 * 证券类型
 *
 * @author yuanhongbo
 */
public enum StockType {
    /**
     * 股票
     */
    STOCK("stock", "场内证券"),
    /**
     * 场外基金
     */
    FUND("fund", "场外基金");

    private final String code;
    private final String name;

    StockType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static StockType of(String code) {
        StockType[] values = StockType.values();
        for (StockType val : values) {
            if (val.getCode().equals(code)) {
                return val;
            }
        }
        throw new IllegalArgumentException(code + " not found.");
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code;
    }
}
