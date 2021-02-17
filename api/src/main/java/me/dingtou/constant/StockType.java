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
    STOCK("stock"),
    /**
     * 场外基金
     */
    FUND("fund");

    private final String code;

    StockType(String code) {
        this.code = code;
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
}
