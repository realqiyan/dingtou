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
    SH("sh"),
    /**
     * 深圳交易所
     */
    SZ("sz"),
    /**
     * 基金
     */
    FUND("fund"),
    /**
     * 香港交易所
     */
    HK("hk");

    private final String code;

    Market(String code) {
        this.code = code;
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
}
