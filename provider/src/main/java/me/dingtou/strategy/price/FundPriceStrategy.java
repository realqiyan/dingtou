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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 场外基金价格
 */
@Component
@Slf4j
public class FundPriceStrategy extends BasePriceStrategy {

    // 30秒缓存
    private Cache<String, BigDecimal> cache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Override
    public boolean isMatch(Stock stock) {
        return Market.FUND.equals(stock.getMarket());
    }

    @Override
    public BigDecimal currentPrice(Stock stock) {
        try {
            return cache.get(stock.getStockUniqueKey(), () -> getCurrentFundPrice(stock));
        } catch (ExecutionException e) {
            log.error("get currentPrice error. stock:" + stock, e);
            return null;
        }
    }

    @Override
    public BigDecimal getSettlementPrice(Stock stock, Date date) {
        try {
            List<StockPrice> stockPrices = pullPrices(stock, date, 1);
            if (null != stockPrices && !stockPrices.isEmpty()) {
                StockPrice stockPrice = stockPrices.get(0);
                if (ChronoUnit.DAYS.between(stockPrice.getDate().toInstant(), date.toInstant()) == 0) {
                    return stockPrice.getPrice();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    public List<StockPrice> pullPrices(Stock stock, Date date, int x) {
        try {
            Date now = new Date();
            long between = Math.abs(ChronoUnit.DAYS.between(now.toInstant(), date.toInstant()));
            x += between;
            List<StockPrice> prices = new ArrayList<StockPrice>();
            String content = StockInfoGetClients.getUrlContent(String.format("https://fundmobapi.eastmoney.com/FundMApi/FundNetDiagram.ashx?deviceid=h5&version=1.2&product=EFund&plat=Wap&FCODE=%s&pageIndex=1&pageSize=%s&_=%s", stock.getCode(), x, System.currentTimeMillis()));
            JSONObject fundData = JSON.parseObject(content);
            JSONArray datas = fundData.getJSONArray("Datas");
            if (null != datas) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                List<StockPrice> finalPrices = prices;
                datas.stream().forEach(data -> {
                    JSONObject jsonData = (JSONObject) data;
                    StockPrice price = new StockPrice();
                    price.setStock(stock);
                    try {
                        price.setDate(sdf.parse(jsonData.getString("FSRQ")));
                    } catch (ParseException e) {
                        throw new RuntimeException("参数异常");
                    }
                    price.setPrice(new BigDecimal(jsonData.getString("DWJZ")));
                    price.setRehabPrice(new BigDecimal(jsonData.getString("LJJZ")));
                    finalPrices.add(price);
                });
                prices = finalPrices.stream()
                        .filter(e -> ChronoUnit.DAYS.between(e.getDate().toInstant(), date.toInstant()) >= 0)
                        .collect(Collectors.toList());
                return prices;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    private BigDecimal getCurrentFundPrice(Stock stock) {
        try {
            String url = String.format("https://fundmobapi.eastmoney.com/FundMApi/FundBasicInformation.ashx?FCODE=%s&deviceid=h5&plat=Wap&product=EFund&version=1.2", stock.getCode());
            String content = StockInfoGetClients.getUrlContent(url);
            JSONObject fundData = JSON.parseObject(content);
            JSONObject data = fundData.getJSONObject("Datas");
            String price = data.getString("DWJZ");
            Date fsrq = data.getDate("FSRQ");
            if (null != fsrq && DateUtils.isSameDay(fsrq, new Date())) {
                return new BigDecimal(price);
            } else {
                fsrq = new Date();
            }
            url = String.format("https://fundmobapi.eastmoney.com/FundMApi/FundVarietieValuationDetail.ashx?FCODE=%s&deviceid=h5&plat=Wap&product=EFund&version=1.2", stock.getCode());
            content = StockInfoGetClients.getUrlContent(url);
            fundData = JSON.parseObject(content);
            JSONObject expansion = fundData.getJSONObject("Expansion");
            String gz = expansion.getString("GZ");
            if (StringUtils.isBlank(gz)) {
                return new BigDecimal(price);
            }
            Date gztime = DateUtils.parseDate(expansion.getString("GZTIME"), "yyyy-MM-dd HH:mm");

            if (null != fsrq && DateUtils.isSameDay(fsrq, gztime)) {
                return new BigDecimal(price);
            }

            fsrq = DateUtils.addHours(fsrq, 15);
            fsrq = DateUtils.addSeconds(fsrq, 1);
            if (fsrq.before(gztime)) {
                return new BigDecimal(gz);
            }

            return new BigDecimal(price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
