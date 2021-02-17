package me.dingtou.strategy.trade;

import me.dingtou.constant.StockType;
import me.dingtou.manager.PriceManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeCfg;
import me.dingtou.model.TradeDetail;
import me.dingtou.strategy.TradeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(AverageValueTradeStrategy.CODE)
public class AverageValueTradeStrategy implements TradeStrategy {

    /**
     * 策略编码
     */
    public static final String CODE = "AverageValueTradeStrategy";

    /**
     * 当前目标价值
     */
    public static final String CURRENT_TARGET_VALUE_KEY = "currentTargetValue";

    /**
     * AVERAGE_STRATEGY 均线策略 key:均线 value:低于均线后金额上浮乘数
     */
    public static final Map<Integer, Double> AVERAGE_STRATEGY = new ConcurrentHashMap<>(4, 1.0f);

    static {
        // 低于30日均线 目标增量1.5倍
        AVERAGE_STRATEGY.put(30, 1.5);
        // 低于60日均线 目标增量2倍
        AVERAGE_STRATEGY.put(60, 2.0);
        // 低于120日均线 目标增量2.5倍
        AVERAGE_STRATEGY.put(120, 2.5);
    }

    @Autowired
    private PriceManager priceManager;

    @Override
    public boolean isMatch(Stock stock) {
        return CODE.equals(stock.getTradeCfg().getTradeStrategy());
    }

    @Override
    public TradeDetail calculateConform(Stock stock, Date date) {

        // 当前价格
        BigDecimal currentPrice = priceManager.getCurrentPrice(stock);
        // 当前份额
        BigDecimal amount = stock.getAmount();
        // 当前价值
        BigDecimal currentValue = amount.multiply(currentPrice);
        currentValue = currentValue.setScale(2, RoundingMode.HALF_UP);

        TradeCfg tradeCfg = stock.getTradeCfg();

        // 已经投入部分的目标价值
        Map<String, String> attributes = tradeCfg.getAttributes();
        if (null == attributes) {
            attributes = new HashMap<>();
            tradeCfg.setAttributes(attributes);
        }
        BigDecimal currentTargetValue = BigDecimal.valueOf(0);
        if (attributes.containsKey(CURRENT_TARGET_VALUE_KEY)) {
            currentTargetValue = new BigDecimal(attributes.get(CURRENT_TARGET_VALUE_KEY));
        }

        // 默认步长
        BigDecimal increment = tradeCfg.getIncrement();

        Date now = new Date();
        // 计算均线平均价格
        int strategySize = AVERAGE_STRATEGY.size();
        // 均线&价格
        final Map<Integer, BigDecimal> average = new HashMap<>(strategySize);
        for (Iterator<Integer> i = AVERAGE_STRATEGY.keySet().iterator(); i.hasNext(); ) {
            Integer averageVal = i.next();
            average.put(averageVal, priceManager.getSmaPrice(stock, now, averageVal));
        }

        // 优先使用更大购买金额
        for (Iterator<Integer> i = average.keySet().iterator(); i.hasNext(); ) {
            Integer averageVal = i.next();
            BigDecimal linePrice = average.get(averageVal);
            if (currentPrice.doubleValue() >= linePrice.doubleValue()) {
                continue;
            }
            // 均线&倍率
            Double multiplyVal = AVERAGE_STRATEGY.get(averageVal);
            BigDecimal newAdjustIncrement = increment.multiply(BigDecimal.valueOf(multiplyVal));
            if (newAdjustIncrement.compareTo(increment) > 0) {
                // 调整后步长
                increment = newAdjustIncrement;
            }
        }

        // 目标价值=上期目标价值+increment
        BigDecimal targetValue = currentTargetValue.add(increment);
        attributes.put(CURRENT_TARGET_VALUE_KEY, targetValue.toPlainString());

        // 交易金额
        BigDecimal tradeFee = targetValue.subtract(currentValue);
        // 交易份额
        BigDecimal tradeAmount = tradeFee.divide(currentPrice, 2, BigDecimal.ROUND_HALF_UP);


        // 最小交易份额
        BigDecimal minTradeAmount = tradeCfg.getMinTradeAmount();
        if (tradeAmount.doubleValue() < minTradeAmount.doubleValue()) {
            tradeAmount = minTradeAmount;
            tradeFee = currentPrice.multiply(tradeAmount);
        }

        // 只能交易最小交易份额的整数倍
        BigDecimal[] bigDecimals = tradeAmount.divideAndRemainder(minTradeAmount);
        BigDecimal remainder = bigDecimals[1];
        if (remainder.doubleValue() > 0) {
            BigDecimal divide = remainder.divide(minTradeAmount, 2, RoundingMode.HALF_UP);
            // 超过10%就加买一手
            if (divide.doubleValue() >= 0.1) {
                BigDecimal multiple = bigDecimals[0].add(BigDecimal.ONE);
                tradeAmount = multiple.multiply(minTradeAmount);
            } else {
                BigDecimal multiple = bigDecimals[0];
                tradeAmount = multiple.multiply(minTradeAmount);
            }
            tradeFee = currentPrice.multiply(tradeAmount);
        }

        //TODO 暂时忽略手续费
        BigDecimal tradeServiceFee = BigDecimal.ZERO;
        return new TradeDetail(targetValue, tradeFee, tradeAmount, tradeServiceFee);
    }

    @Override
    public TradeDetail calculateSettlement(Order order) {
        BigDecimal settlementPrice = priceManager.getSettlementPrice(order.getStock(), order.getTradeTime());
        if (null == settlementPrice) {
            return null;
        }
        BigDecimal tradeFee = order.getTradeFee();
        if (StockType.FUND.equals(order.getStock().getType())) {
            BigDecimal tradeAmount = tradeFee.divide(settlementPrice, 2, RoundingMode.HALF_UP);
            return new TradeDetail(null, tradeFee, tradeAmount, order.getTradeServiceFee());
        }
        return new TradeDetail(null, tradeFee, order.getTradeAmount(), order.getTradeServiceFee());
    }

}
