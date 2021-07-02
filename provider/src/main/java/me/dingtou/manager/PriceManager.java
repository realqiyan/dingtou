package me.dingtou.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;
import me.dingtou.strategy.PriceStrategy;
import me.dingtou.util.StockInfoGetClients;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 价格管理
 */
@Component
public class PriceManager {

    @Resource
    private List<PriceStrategy> priceStrategies;

    /**
     * 拉取当前价格
     *
     * @param stock
     * @return
     */
    public BigDecimal getCurrentPrice(Stock stock) {


        for (PriceStrategy priceStrategy : priceStrategies) {
            if (priceStrategy.isMatch(stock)) {
                return priceStrategy.currentPrice(stock);
            }
        }

        throw new NullPointerException("PriceStrategy not found. ");
    }

    /**
     * 获取结算金额
     *
     * @param stock
     * @param date
     * @return
     */
    public BigDecimal getSettlementPrice(Stock stock, Date date) {


        for (PriceStrategy priceStrategy : priceStrategies) {
            if (priceStrategy.isMatch(stock)) {
                return priceStrategy.getSettlementPrice(stock, date);
            }
        }

        throw new NullPointerException("PriceStrategy not found. ");
    }

    /**
     * 拉取价格
     *
     * @param stock
     * @param date
     * @param x
     * @return
     */
    public List<StockPrice> getPrices(Stock stock, Date date, int x) {
        for (PriceStrategy priceStrategy : priceStrategies) {
            if (priceStrategy.isMatch(stock)) {
                List<StockPrice> stockPrices = priceStrategy.listPrice(stock, date, x);
                // 结果按照时间排序
                stockPrices.sort((o1, o2) -> {
                    if (null == o1 || null == o2) {
                        return 0;
                    }
                    return o1.getDate().compareTo(o2.getDate());
                });
                return stockPrices;
            }
        }
        throw new NullPointerException("PriceStrategy not found. ");
    }

    /**
     * 拉取指数估值比例
     *
     * @param targetIndexCode
     * @return
     */
    public BigDecimal getIndexValuationRatio(String targetIndexCode) {
        if (null == targetIndexCode) {
            return null;
        }
        String url = String.format("https://danjuanapp.com/djapi/index_eva/detail/%s", targetIndexCode.trim().toUpperCase());
        String content = StockInfoGetClients.getUrlContent(url);
        JSONObject jsonObject = JSON.parseObject(content);
        JSONObject data = jsonObject.getJSONObject("data");
        if (null == data) {
            return null;
        }
        return data.getBigDecimal("pe_percentile");
    }
}
