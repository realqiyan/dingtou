package me.dingtou.manager;

import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;
import me.dingtou.strategy.PriceStrategy;
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
     * 拉取均价
     *
     * @param stock
     * @param date
     * @param x
     * @return
     */
    public BigDecimal getSmaPrice(Stock stock, Date date, int x) {
        for (PriceStrategy priceStrategy : priceStrategies) {
            if (priceStrategy.isMatch(stock)) {
                return priceStrategy.smaPrice(stock, date, x);
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
                return priceStrategy.listPrice(stock, date, x);
            }
        }
        throw new NullPointerException("PriceStrategy not found. ");
    }

}
