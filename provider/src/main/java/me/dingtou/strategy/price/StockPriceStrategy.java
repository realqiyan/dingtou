package me.dingtou.strategy.price;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.Market;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;
import me.dingtou.util.StockInfoGetClients;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A股价格
 */
@Slf4j
@Component
public class StockPriceStrategy extends BasePriceStrategy {

    // 30秒缓存
    private Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Override
    public boolean isMatch(Stock stock) {
        return Market.SH.equals(stock.getMarket()) || Market.SZ.equals(stock.getMarket());
    }

    @Override
    public BigDecimal currentPrice(Stock stock) {
        if (Market.SH.equals(stock.getMarket())) {
            return getStockPrice(String.format("http://qt.gtimg.cn/q=sh%s", stock.getCode()));
        } else if (Market.SZ.equals(stock.getMarket())) {
            return getStockPrice(String.format("http://qt.gtimg.cn/q=sz%s", stock.getCode()));
        }
        return null;
    }

    @Override
    public BigDecimal getSettlementPrice(Stock stock, Date date) {
        try {
            List<StockPrice> stockPrices = pullPrices(stock, date, 1);
            if (null != stockPrices && !stockPrices.isEmpty()) {
                return stockPrices.get(0).getPrice();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<StockPrice> pullPrices(Stock stock, Date date, int x) {
        // 计算date和当前时间的时间差 x+上时间差
        Date now = new Date();
        long between = Math.abs(ChronoUnit.DAYS.between(now.toInstant(), date.toInstant()));
        x += between;
        //日K:https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?symbol=sz000002&scale=240&ma=no&datalen=30
        List<StockPrice> stockPrices = null;
        if (Market.SH.equals(stock.getMarket())) {
            stockPrices = pullStockPrice(stock, String.format("https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?symbol=sh%s&scale=240&ma=no&datalen=%s", stock.getCode(), x));
        } else if (Market.SZ.equals(stock.getMarket())) {
            stockPrices = pullStockPrice(stock, String.format("https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?symbol=sz%s&scale=240&ma=no&datalen=%s", stock.getCode(), x));
        }
        if (null != stockPrices) {
            stockPrices = stockPrices.stream()
                    .filter(e -> ChronoUnit.DAYS.between(e.getDate().toInstant(), date.toInstant()) >= 0)
                    .collect(Collectors.toList());
        }
        return stockPrices;
    }


    private BigDecimal getStockPrice(String url) {
        String urlContent = null;
        try {
            urlContent = cache.get(url, () -> StockInfoGetClients.getUrlContent(url));
            String[] strings = urlContent.split("~");
            return new BigDecimal(strings[3]);
        } catch (Exception e) {
            log.error("getUrlContent error.url:" + url, e);
            return null;
        }

    }

    private List<StockPrice> pullStockPrice(Stock stock, String url) {
        try {
            List<StockPrice> prices = new ArrayList<StockPrice>();
            String content = StockInfoGetClients.getUrlContent(url);
            JSONArray priceList = JSON.parseArray(content);
            if (null != priceList) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                priceList.stream().forEach(data -> {
                    JSONObject jsonData = (JSONObject) data;
                    StockPrice price = new StockPrice();
                    price.setStock(stock);
                    try {
                        price.setDate(sdf.parse(jsonData.getString("day")));
                    } catch (ParseException e) {
                        throw new RuntimeException("参数异常");
                    }
                    price.setPrice(new BigDecimal(jsonData.getString("close")));
                    prices.add(price);
                });
                return prices;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
