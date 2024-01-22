package me.dingtou.strategy.price;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.Market;
import me.dingtou.model.Stock;
import me.dingtou.model.StockAdjust;
import me.dingtou.model.StockPrice;
import me.dingtou.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A股价格
 */
@Slf4j
@Component
public class StockPriceStrategy extends BasePriceStrategy {

    private static final String QFQ_DATA_PATTERN = "(\\{.*\\})";
    // 30秒缓存
    private final Cache<String, String> cache = CacheBuilder.newBuilder()
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
            return getStockPrice(String.format("https://forward.myworker.win/http://qt.gtimg.cn/q=sh%s", stock.getCode()));
        } else if (Market.SZ.equals(stock.getMarket())) {
            return getStockPrice(String.format("https://forward.myworker.win/http://qt.gtimg.cn/q=sz%s", stock.getCode()));
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


    /**
     * 获取指定股票在指定时间范围内的时间
     *
     * @param stock    股票对象
     * @param date     开始日期
     * @param timeSpan 时间跨度
     * @return 指定股票在指定时间范围内的价格列表
     */
    @Override
    public List<StockPrice> pullPrices(Stock stock, Date date, int timeSpan) {
        // 计算date和当前时间的时间差 timeSpan+上时间差
        Date now = new Date();
        long between = Math.abs(ChronoUnit.DAYS.between(now.toInstant(), date.toInstant()));
        timeSpan += (int) between;
        List<StockPrice> stockPrices = pullStockPrice(stock, timeSpan);
        if (null != stockPrices && !stockPrices.isEmpty()) {
            stockPrices = stockPrices.stream()
                    .filter(e -> ChronoUnit.DAYS.between(e.getDate().toInstant(), date.toInstant()) >= 0)
                    .collect(Collectors.toList());
        }
        return stockPrices;
    }


    private BigDecimal getStockPrice(String url) {
        String urlContent = null;
        try {
            urlContent = cache.get(url, () -> HttpUtils.getUrlContent(url));
            String[] strings = urlContent.split("~");
            return new BigDecimal(strings[3]);
        } catch (Exception e) {
            log.error("getUrlContent error.url:" + url, e);
            return null;
        }

    }

    private List<StockPrice> pullStockPrice(Stock stock, int timeSpan) {
        try {
            //日K:https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?symbol=sz000002&scale=240&ma=no&datalen=30
            String symbol = (stock.getMarket().equals(Market.SH) ? "sh" : "sz") + stock.getCode();
            String historyApiUrl = String.format("https://forward.myworker.win/https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?symbol=%s&scale=240&ma=no&datalen=%s", symbol, timeSpan);

            //前复权:https://finance.sina.com.cn/realstock/company/sz000002/qfq.js
            String adjustApiUrl = String.format("https://forward.myworker.win/https://finance.sina.com.cn/realstock/company/%s/qfq.js", symbol);

            List<StockPrice> prices = new ArrayList<StockPrice>();
            String historyContent = HttpUtils.getUrlContent(historyApiUrl);
            JSONArray priceList = JSON.parseArray(historyContent);
            if (null != priceList) {

                List<StockAdjust> adjustList = new ArrayList<StockAdjust>();
                // 前复权数据
                String adjustContent = HttpUtils.getUrlContent(adjustApiUrl);
                if (!StringUtils.isBlank(adjustContent)) {
                    Pattern qfq = Pattern.compile(QFQ_DATA_PATTERN);
                    Matcher m = qfq.matcher(adjustContent);
                    if (m.find()) {
                        JSONObject adjustJson = JSON.parseObject(m.group(0));
                        JSONArray qfqValues = adjustJson.getJSONArray("data");
                        if (null != qfqValues) {
                            qfqValues.forEach(qfqValue -> {
                                JSONObject qfqJson = (JSONObject) qfqValue;
                                Date adjustDate = qfqJson.getDate("d");
                                BigDecimal adjustVal = new BigDecimal(qfqJson.getString("f"));
                                StockAdjust adjust = new StockAdjust(stock.getCode(), adjustDate, adjustVal);
                                adjustList.add(adjust);
                            });
                            adjustList.sort(StockAdjust::compareTo);
                        }
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                priceList.forEach(data -> {
                    JSONObject jsonData = (JSONObject) data;
                    StockPrice price = new StockPrice();
                    price.setStock(stock);
                    try {
                        price.setDate(sdf.parse(jsonData.getString("day")));
                    } catch (ParseException e) {
                        throw new RuntimeException("参数异常");
                    }
                    price.setPrice(new BigDecimal(jsonData.getString("close")));
                    price.setRehabPrice(adjust(price, adjustList));
                    prices.add(price);
                });
                return prices;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 根据调整列表对股票价格进行调整
     *
     * @param price       股票价格对象
     * @param adjustList  调整列表
     * @return  返回调整后的价格，如果不符合任何调整则返回null
     */
    private BigDecimal adjust(StockPrice price, List<StockAdjust> adjustList) {
        for (StockAdjust adjust : adjustList) {
            if (price.getDate().after(adjust.getAdjustDate()) || DateUtils.isSameDay(price.getDate(), adjust.getAdjustDate())) {
                return price.getPrice().divide(adjust.getAdjustVal(), 2, RoundingMode.FLOOR);
            }
        }
        return null;
    }


    public static void main(String[] args) {
        Stock stock = new Stock();
        stock.setCode("000002");
        stock.setMarket(Market.SZ);
        List<StockPrice> stockPrices = new StockPriceStrategy().pullStockPrice(stock, 100);
        assert stockPrices != null;
        stockPrices.forEach(e -> System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(e.getDate()) + " " + e.getRehabPrice()));
    }
}
