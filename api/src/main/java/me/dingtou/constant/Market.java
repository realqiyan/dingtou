package me.dingtou.constant;

/**
 * 市场
 *
 * @author yuanhongbo
 */
public enum Market {
    /**
     * 上海交易所
     */
    SH("sh", "上交所"),
    /**
     * 深圳交易所
     */
    SZ("sz", "深交所"),
    /**
     * 基金
     */
    FUND("fund", "场外基金"),
    /**
     * 香港交易所
     */
    HK("hk", "港交所");

    private final String code;
    private final String name;

    Market(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Market of(String code) {
        Market[] values = Market.values();
        for (Market val : values) {
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
