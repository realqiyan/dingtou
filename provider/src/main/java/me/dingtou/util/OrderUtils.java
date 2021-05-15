package me.dingtou.util;

import me.dingtou.constant.StockType;
import me.dingtou.model.Stock;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * 订单工具
 */
public class OrderUtils {

    /**
     * 获取证券下一次交易时间
     *
     * @param stock
     * @param now
     * @return
     */
    public static Date getNextTradeTime(Stock stock, Date now) {
        Date tradeTime = now;
        if (StockType.FUND.equals(stock.getType())) {
            try {
                String tradeCron = stock.getTradeCfg().getTradeCron();
                if (null != tradeCron) {
                    CronExpression cron = new CronExpression(tradeCron);
                    tradeTime = cron.getNextValidTimeAfter(now);
                }
            } catch (Exception e) {
                tradeTime = now;
            }
        }
        return DateUtils.truncate(tradeTime, Calendar.DATE);
    }

}
