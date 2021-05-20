package me.dingtou.strategy.trade;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.StockType;
import me.dingtou.manager.PriceManager;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeCfg;
import me.dingtou.model.TradeDetail;
import me.dingtou.strategy.TradeStrategy;
import me.dingtou.util.OrderUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 价值平均定投策略增强版（增加上浮比例）
 * <pre>
 * 当前持有价值=当前基金持有份额*当前基金价格
 * 定投目标价值=本次定投结束后所持有基金的总价值
 * 上期目标价值=上一期计算出来的定投目标价值
 *
 * 定投目标价值=上期目标价值+首次定投金额*(1+上浮比例)
 * 说明:首次定投时“上期目标价值 ”为0
 *
 * 本期定投金额=定投目标价值-当前持有价值
 * </pre>
 */
@Slf4j
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
     * 上一期目标价值
     */
    public static final String PRE_TARGET_VALUE_KEY = "preTargetValue";

    /**
     * 目标指数编码
     */
    public static final String TARGET_INDEX_CODE = "targetIndexCode";

    /**
     * 目标指数估值
     */
    public static final String CURRENT_TARGET_INDEX_VALUATION = "currentTargetIndexValuation";

    /**
     * 最大目标价值
     */
    public static final String MAX_TARGET_VALUE_KEY = "maxTargetValue";

    /**
     * 配置0日均线代表调整默认倍率（默认倍率1） 浮动策略： {120:"2",60:"1.5",30:"1",0:"0"}
     */
    public static final String AVERAGE_STRATEGY_KEY = "averageStrategy";

    /**
     * AVERAGE_STRATEGY 均线策略 key:均线 value:低于均线后金额上浮乘数
     */
    public static final Map<Integer, String> DEFAULT_AVERAGE_STRATEGY = new HashMap<>(4, 1.0f);

    /**
     * 1必须足额才能买卖，不支持买入卖出"四舍五入" 0.5就支持四舍五入
     */
    public static final double OVER_RATIO = 1.0;

    static {
        // 低于30日均线 目标增量1.5倍
        DEFAULT_AVERAGE_STRATEGY.put(30, "1.5");
        // 低于60日均线 目标增量2倍
        DEFAULT_AVERAGE_STRATEGY.put(60, "2.0");
        // 低于120日均线 目标增量2.5倍
        DEFAULT_AVERAGE_STRATEGY.put(120, "2.5");
    }

    @Resource
    private PriceManager priceManager;

    @Override
    public boolean isMatch(Stock stock) {
        return CODE.equals(stock.getTradeCfg().getTradeStrategy());
    }

    @Override
    public TradeDetail calculateConform(Stock stock, Date date) {
        TradeCfg tradeCfg = stock.getTradeCfg();
        // 策略扩展参数初始化
        Map<String, String> attributes = tradeCfg.getAttributes();
        if (null == attributes) {
            attributes = new HashMap<>();
            tradeCfg.setAttributes(attributes);
        }
        // 已经投入部分的目标价值
        BigDecimal currentTargetValue = BigDecimal.valueOf(0);
        if (attributes.containsKey(CURRENT_TARGET_VALUE_KEY)) {
            currentTargetValue = new BigDecimal(attributes.get(CURRENT_TARGET_VALUE_KEY));
        }
        attributes.put(PRE_TARGET_VALUE_KEY, currentTargetValue.toPlainString());

        // 已经买过就跳过计算
        Date lastTradeTime = stock.getLastTradeTime();
        if (null != lastTradeTime && DateUtils.isSameDay(lastTradeTime, OrderUtils.getNextTradeTime(stock, date))) {
            return new TradeDetail(currentTargetValue, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        // 当前价格
        BigDecimal currentPrice = priceManager.getCurrentPrice(stock);
        // 当前份额
        BigDecimal amount = stock.getAmount();
        // 当前价值
        BigDecimal currentValue = amount.multiply(currentPrice);
        currentValue = currentValue.setScale(2, RoundingMode.HALF_UP);

        // 翻倍策略
        Map<Integer, String> averageStrategy = new HashMap<>();
        if (attributes.containsKey(AVERAGE_STRATEGY_KEY)) {
            try {
                averageStrategy.putAll(JSON.parseObject(attributes.get(AVERAGE_STRATEGY_KEY), Map.class));
            } catch (Exception e) {
                log.error("build averageStrategy error. attributes:" + attributes, e);
                averageStrategy.putAll(DEFAULT_AVERAGE_STRATEGY);
            }
        } else {
            averageStrategy.putAll(DEFAULT_AVERAGE_STRATEGY);
        }
        attributes.put(AVERAGE_STRATEGY_KEY, JSON.toJSONString(averageStrategy));

        // 计算步长
        BigDecimal increment = calculateIncrement(stock, currentPrice, averageStrategy);

        // 目标价值=上期目标价值+increment
        BigDecimal targetValue = currentTargetValue.add(increment);

        // 买入上限
        if (attributes.containsKey(MAX_TARGET_VALUE_KEY)) {
            BigDecimal maxTargetValue = new BigDecimal(attributes.get(MAX_TARGET_VALUE_KEY));
            if (targetValue.compareTo(maxTargetValue) > 0) {
                log.info("stock:{},maxTargetValue:{},targetValue:{}", stock.getCode(), maxTargetValue, targetValue);
                targetValue = maxTargetValue;
            }
        }

        attributes.put(CURRENT_TARGET_VALUE_KEY, targetValue.toPlainString());

        // 交易金额
        BigDecimal tradeFee = targetValue.subtract(currentValue);
        // 交易份额
        BigDecimal tradeAmount = tradeFee.divide(currentPrice, 2, BigDecimal.ROUND_HALF_UP);
        // 最小交易份额（一手）
        BigDecimal minTradeAmount = tradeCfg.getMinTradeAmount();
        // 只能交易最小交易份额的整数倍
        BigDecimal[] bigDecimals = tradeAmount.divideAndRemainder(minTradeAmount);
        BigDecimal remainder = bigDecimals[1];
        if (remainder.doubleValue() != 0) {
            BigDecimal divide = remainder.divide(minTradeAmount, 2, RoundingMode.HALF_UP);
            // 超过比例就加买/加卖一手
            if (Math.abs(divide.doubleValue()) >= OVER_RATIO) {
                BigDecimal multiple;
                if (bigDecimals[0].doubleValue() >= 0) {
                    multiple = bigDecimals[0].add(BigDecimal.ONE);
                } else {
                    multiple = bigDecimals[0].subtract(BigDecimal.ONE);
                }
                tradeAmount = multiple.multiply(minTradeAmount);
            } else {
                BigDecimal multiple = bigDecimals[0];
                tradeAmount = multiple.multiply(minTradeAmount);
            }
            tradeFee = currentPrice.multiply(tradeAmount);
        }
        // 手续费
        BigDecimal tradeServiceFee = BigDecimal.ZERO;
        if (tradeFee.doubleValue() != 0) {
            if (null != tradeCfg.getServiceFeeRate()) {
                tradeServiceFee = tradeFee.abs().multiply(tradeCfg.getServiceFeeRate());
                tradeServiceFee = tradeServiceFee.setScale(2, RoundingMode.HALF_DOWN);
            }
            if (null != tradeCfg.getMinServiceFee() && tradeServiceFee.doubleValue() < tradeCfg.getMinServiceFee().doubleValue()) {
                tradeServiceFee = tradeCfg.getMinServiceFee();
            }
        }
        return new TradeDetail(targetValue, tradeFee, tradeAmount, tradeServiceFee);
    }

    /**
     * 计算下一期买入金额
     *
     * @param stock
     * @param currentPrice
     * @param averageStrategy
     * @return
     */
    private BigDecimal calculateIncrement(Stock stock, BigDecimal currentPrice, Map<Integer, String> averageStrategy) {
        TradeCfg tradeCfg = stock.getTradeCfg();
        // 默认步长
        BigDecimal increment = tradeCfg.getIncrement();

        // 跟踪的指数估值
        String targetIndexCode = tradeCfg.getAttributes().get(TARGET_INDEX_CODE);
        if (null != targetIndexCode) {
            BigDecimal indexValuationRatio = priceManager.getIndexValuationRatio(targetIndexCode);
            if (null != indexValuationRatio) {
                tradeCfg.getAttributes().put(CURRENT_TARGET_INDEX_VALUATION, indexValuationRatio.toPlainString());
                // 估值水位80%～100% 0倍
                // 估值水位60%～80%  0.5倍
                // 估值水位20%～60%  1倍
                // 估值水位 0%～20%  1.5倍
                if (indexValuationRatio.doubleValue() > 0.8) {
                    increment = BigDecimal.ZERO;
                } else if (indexValuationRatio.doubleValue() > 0.6) {
                    increment = increment.multiply(new BigDecimal("0.5"));
                } else if (indexValuationRatio.doubleValue() > 0.2) {
                    increment = increment.multiply(new BigDecimal("1"));
                } else if (indexValuationRatio.doubleValue() >= 0.0) {
                    increment = increment.multiply(new BigDecimal("1.5"));
                }
            }
        }


        Date now = new Date();
        // 计算均线平均价格
        int strategySize = averageStrategy.size();
        // 均线&价格
        final Map<Integer, BigDecimal> average = new HashMap<>(strategySize);
        for (Iterator<Integer> i = averageStrategy.keySet().iterator(); i.hasNext(); ) {
            Integer averageVal = i.next();
            average.put(averageVal, priceManager.getSmaPrice(stock, now, averageVal));
        }

        // 优先使用长期均线比较，优先使用长期均线价格
        List<Integer> averageDays = Lists.newArrayList(average.keySet().iterator());
        Collections.sort(averageDays, Comparator.reverseOrder());
        for (Integer averageVal : averageDays) {
            BigDecimal linePrice = average.get(averageVal);
            // 配置0日均线代表调整默认倍率（默认倍率1）
            if (averageVal.intValue() != 0 && currentPrice.doubleValue() >= linePrice.doubleValue()) {
                continue;
            }
            // 均线&倍率
            String multiplyVal = averageStrategy.get(averageVal);
            log.info("stock:{},averageDay:{},multiplyVal:{}", stock.getCode(), averageVal, multiplyVal);
            increment = increment.multiply(new BigDecimal(multiplyVal));
            break;
        }
        return increment;
    }

    @Override
    public TradeDetail calculateSettlement(Order order) {
        BigDecimal settlementPrice = priceManager.getSettlementPrice(order.getStock(), order.getTradeTime());
        if (null == settlementPrice) {
            return null;
        }
        BigDecimal tradeFee = order.getTradeFee();
        BigDecimal tradeServiceFee = order.getTradeServiceFee();
        if (StockType.FUND.equals(order.getStock().getType())) {
            // 减去手续费后实际用于购买份额的金额
            BigDecimal realTradeFee = tradeFee.subtract(tradeServiceFee);
            BigDecimal tradeAmount = realTradeFee.divide(settlementPrice, 2, BigDecimal.ROUND_HALF_DOWN);
            return new TradeDetail(null, tradeFee, tradeAmount, tradeServiceFee);
        }
        return new TradeDetail(null, tradeFee, order.getTradeAmount(), tradeServiceFee);
    }
}
