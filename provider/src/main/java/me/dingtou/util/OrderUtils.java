package me.dingtou.util;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 订单工具
 */
public class OrderUtils {

    /**
     * 获取交易日
     *
     * @param tradeTime
     * @return
     */
    public static Date getTradeDate(Date tradeTime) {
        return DateUtils.truncate(tradeTime, Calendar.DATE);
    }

}
