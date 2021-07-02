package me.dingtou.strategy.price;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;
import me.dingtou.strategy.PriceStrategy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class BasePriceStrategy implements PriceStrategy {

    private static Cache<String, List<StockPrice>> PRICE_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @Override
    public final List<StockPrice> listPrice(Stock stock, Date date, int x) {
        // 统一处理缓存 同一请求1分钟请求一次
        String cacheKey = String.format("%s_%s_%s", stock.getMarket().getCode(), stock.getCode(), x);
        try {
            return PRICE_CACHE.get(cacheKey, () -> pullPrices(stock, date, x));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拉取价格
     *
     * @param stock
     * @param date
     * @param x
     * @return
     */
    protected abstract List<StockPrice> pullPrices(Stock stock, Date date, int x);

}
