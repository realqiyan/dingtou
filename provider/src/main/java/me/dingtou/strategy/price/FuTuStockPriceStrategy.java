package me.dingtou.strategy.price;

import com.futu.openapi.pb.QotCommon;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.Market;
import me.dingtou.model.Stock;
import me.dingtou.model.StockPrice;
import me.dingtou.strategy.price.futu.FuTuAPI;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 股价
 */
@Component
@Slf4j
public class FuTuStockPriceStrategy extends StockPriceStrategy {

    @Override
    public boolean isMatch(Stock stock) {
        return Market.SH.equals(stock.getMarket()) || Market.SZ.equals(stock.getMarket()) || Market.HK.equals(stock.getMarket());
    }

    @Override
    public BigDecimal currentPrice(Stock stock) {
        try {
            double price = FuTuAPI.getInstance().syncGetBasicQot(stock.getMarket(), stock.getCode());
            return BigDecimal.valueOf(price);
        } catch (Exception e) {
            log.error("futu currentPrice error. message:" + e.getMessage());
            return super.currentPrice(stock);
        }
    }

    @Override
    public BigDecimal getSettlementPrice(Stock stock, Date date) {
        try {
            List<StockPrice> stockPrices = pullPrices(stock, date, 1);
            if (null != stockPrices && !stockPrices.isEmpty()) {
                return stockPrices.get(0).getPrice();
            }
        } catch (Exception e) {
            log.error("futu getSettlementPrice error. message:" + e.getMessage());
            return super.getSettlementPrice(stock, date);
        }
        return null;
    }

    @Override
    public List<StockPrice> pullPrices(Stock stock, Date date, int x) {
        try {
            // 计算date和当前时间的时间差 x+上时间差
            Date now = new Date();
            int between = (int) Math.abs(ChronoUnit.DAYS.between(now.toInstant(), date.toInstant()));
            int num = (x + between);
            List<StockPrice> stockPrices = pullStockPrice(stock, num);
            if (null != stockPrices) {
                stockPrices = stockPrices.stream()
                        .filter(e -> ChronoUnit.DAYS.between(e.getDate().toInstant(), date.toInstant()) >= 0)
                        .collect(Collectors.toList());
            }
            return stockPrices;
        } catch (Exception e) {
            log.error("futu pullPrices error. message:" + e.getMessage());
            return super.pullPrices(stock, date, x);
        }
    }


    private List<StockPrice> pullStockPrice(Stock stock, int num) {
        try {
            List<StockPrice> prices = new ArrayList<StockPrice>();
            List<QotCommon.KLine> kLines = FuTuAPI.getInstance().syncGetKL(stock.getMarket(), stock.getCode(), num);
            if (null != kLines) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                kLines.stream().forEach(data -> {
                    StockPrice price = new StockPrice();
                    price.setStock(stock);
                    try {
                        price.setDate(sdf.parse(data.getTime()));
                    } catch (ParseException e) {
                        throw new RuntimeException("参数异常");
                    }
                    price.setPrice(BigDecimal.valueOf(data.getClosePrice()));
                    price.setRehabPrice(BigDecimal.valueOf(data.getClosePrice()));
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
