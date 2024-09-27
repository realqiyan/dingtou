package me.dingtou.strategy.trade;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.OrderSnapshotKeys;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.constant.TradeType;
import me.dingtou.manager.PriceManager;
import me.dingtou.model.*;
import me.dingtou.strategy.TradeStrategy;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
     * 本次交易价格
     */
    public static final String CURRENT_TRADE_PRICE = "currentTradePrice";
    /**
     * 本次增量价格
     */
    public static final String CURRENT_INCREMENT = "currentIncrement";
    /**
     * 单次最大交易价格
     */
    public static final String PER_MAX_TRADE_PRICE = "perMaxTradePrice";
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
     * sma策略 配置说明：均线数量n|均线1,均线2,均线n|高于1条均线购买比例,高于2条均线购买比例,高于n条均线购买比例 例如：4|10,30,60,120|1.5,1.25,1,0
     * 注意：自由落体趋势会暂停购买（低于所有均线不买入）
     */
    public static final String SMA_STRATEGY_KEY = "smaStrategy";
    public static final String SMA_STRATEGY_DEFAULT_VALUE = "4|10,30,60,120|1.5,1.25,1,0";
    /**
     * 1必须足额才能买卖，不支持买入卖出"四舍五入" 0.5就支持四舍五入
     */
    public static final double OVER_RATIO = 1.0;
    /**
     * 卖出时，当前价格涨幅少于比例不卖出 15%
     */
    public static final double SELL_PROFIT_RATIO = 0.15;
    @Autowired
    private PriceManager priceManager;

    @Override
    public boolean isMatch(Stock stock) {
        return CODE.equals(stock.getTradeCfg().getTradeStrategy());
    }

    @Override
    public TradeDetail calculateConform(Stock stock, List<Order> stockOrders, Date date) {
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
        // 冗余记录上期目标价值
        attributes.put(PRE_TARGET_VALUE_KEY, currentTargetValue.toPlainString());
        // 当天已经买过就跳过计算
        Optional<Order> any = stockOrders.stream()
                .filter(order -> DateUtils.isSameDay(order.getTradeTime(), date))
                .findAny();
        if (any.isPresent()) {
            return new TradeDetail(currentTargetValue, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        // 当前价格
        BigDecimal currentPrice = priceManager.getCurrentPrice(stock);
        // 冗余记录实时交易价格
        attributes.put(CURRENT_TRADE_PRICE, currentPrice.toPlainString());
        // 当前总份额
        BigDecimal amount = stock.getAmount();
        // 当前总价值
        BigDecimal currentValue = amount.multiply(currentPrice);
        currentValue = currentValue.setScale(2, RoundingMode.HALF_UP);
        // 均线策略
        String smaStrategyConfig = attributes.getOrDefault(SMA_STRATEGY_KEY, SMA_STRATEGY_DEFAULT_VALUE);
        Pair<List<Integer>, List<BigDecimal>> smaStrategyPair = parseSmaStrategyPair(smaStrategyConfig);
        // 计算步长
        BigDecimal increment = calculateIncrement(stock, currentPrice, smaStrategyPair);
        attributes.put(CURRENT_INCREMENT, increment.toPlainString());
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
        // 冗余记录本次目标价值
        attributes.put(CURRENT_TARGET_VALUE_KEY, targetValue.toPlainString());
        // 交易金额
        BigDecimal tradeFee = targetValue.subtract(currentValue);
        // 最大单次交易金额处理 PER_MAX_TRADE_PRICE
        if (attributes.containsKey(PER_MAX_TRADE_PRICE)) {
            BigDecimal perMaxTradePrice = new BigDecimal(attributes.get(PER_MAX_TRADE_PRICE));
            if (tradeFee.compareTo(perMaxTradePrice) > 0) {
                log.info("stock:{},perMaxTradePrice:{},tradeFee:{}", stock.getCode(), perMaxTradePrice, tradeFee);
                tradeFee = perMaxTradePrice;
            }
        }
        //最总交易金额（tradeFee）如果是买入则继续计算，如果是卖出，就去匹配历史交易订单。
        if (tradeFee.compareTo(BigDecimal.ZERO) > 0) {
            return buy(stock, targetValue, tradeFee, currentPrice);
        } else {
            return sell(stock, stockOrders, targetValue, tradeFee, currentPrice);
        }
    }

    private TradeDetail sell(Stock stock, List<Order> stockOrders, BigDecimal targetValue, BigDecimal tradeFee, BigDecimal currentPrice) {
        // 需要过滤已经卖出的订单
        List<String> orderOutIds = stockOrders.stream()
                .filter(order -> TradeType.SELL.equals(order.getType()))
                .map(Order::getSnapshot)
                .filter(Objects::nonNull)
                .map(snapshot -> snapshot.getOrDefault(OrderSnapshotKeys.BUY_ORDER_OUT_IDS, null))
                .filter(Objects::nonNull)
                .flatMap(outIds -> JSON.parseArray(outIds, String.class).stream())
                .collect(Collectors.toList());

        // 找出可以卖的订单
        BigDecimal sellTotalFee = tradeFee.abs();
        List<Order> orderList = stockOrders.stream()
                .filter(order -> TradeType.BUY.equals(order.getType()))
                .filter(order -> !orderOutIds.contains(order.getOutId()))
                .filter(order -> order.getStatus().equals(TradeStatus.DONE))
                .filter(order -> order.getTradeFee().compareTo(BigDecimal.ZERO) > 0)
                .filter(order -> order.getTradeAmount().multiply(currentPrice).compareTo(sellTotalFee) <= 0)
                .peek(order -> {
                    BigDecimal currentProfitFee = order.getTradeAmount().multiply(currentPrice)
                            .subtract(order.getTradeFee())
                            .subtract(order.getTradeServiceFee());
                    order.setCurrentProfitFee(currentProfitFee);
                    BigDecimal currentProfitRatio = currentProfitFee.divide(order.getTradeFee(), 2, RoundingMode.HALF_UP);
                    order.setCurrentProfitRatio(currentProfitRatio);
                })
                .filter(order -> order.getCurrentProfitRatio().compareTo(BigDecimal.valueOf(SELL_PROFIT_RATIO)) > 0)
                .sorted(Comparator.comparing(Order::getTradeFee))
                .collect(Collectors.toList());

        // 按照金额从小到大排序
        BigDecimal sellTotalAmount = sellTotalFee.divide(currentPrice, 2, RoundingMode.HALF_UP);
        List<Order> sellOrders = new ArrayList<>();
        BigDecimal sellAmount = new BigDecimal(0);
        for (Order order : orderList) {
            BigDecimal tradeAmount = order.getTradeAmount();
            if (tradeAmount.compareTo(sellTotalAmount) > 0 || sellAmount.add(tradeAmount).compareTo(sellTotalAmount) > 0) {
                break;
            }
            sellOrders.add(order);
            sellAmount = sellAmount.add(tradeAmount);
        }

        sellAmount = BigDecimal.ZERO.subtract(sellAmount);
        BigDecimal sellFee = sellAmount.multiply(currentPrice);

        // 手续费
        BigDecimal tradeServiceFee = getTradeServiceFee(sellFee, stock.getTradeCfg());
        return new TradeDetail(targetValue, sellFee, sellAmount, tradeServiceFee, sellOrders);

    }


    private static TradeDetail buy(Stock stock, BigDecimal targetValue, BigDecimal tradeFee, BigDecimal currentPrice) {
        TradeCfg tradeCfg = stock.getTradeCfg();
        // 计算交易份额
        BigDecimal tradeAmount = tradeFee.divide(currentPrice, 2, RoundingMode.HALF_UP);
        // 最小交易份额（一手）
        BigDecimal minTradeAmount = tradeCfg.getMinTradeAmount();
        // 只能交易最小交易份额的整数倍。例如买865股，一手100股，只能买8手或9手。
        BigDecimal[] bigDecimals = tradeAmount.divideAndRemainder(minTradeAmount);
        BigDecimal remainder = bigDecimals[1];
        if (remainder.doubleValue() != 0) {
            // 如果有余数，则需要根据比例进行加买/加卖
            BigDecimal divide = remainder.divide(minTradeAmount, 2, RoundingMode.HALF_UP);
            // 超过比例就加买/加卖一手
            BigDecimal multiple = bigDecimals[0];
            if (Math.abs(divide.doubleValue()) >= OVER_RATIO) {
                multiple = bigDecimals[0].doubleValue() >= 0 ? bigDecimals[0].add(BigDecimal.ONE) : bigDecimals[0].subtract(BigDecimal.ONE);
            }
            tradeAmount = multiple.multiply(minTradeAmount);
            tradeFee = currentPrice.multiply(tradeAmount);
        }
        // 手续费
        BigDecimal tradeServiceFee = getTradeServiceFee(tradeFee, tradeCfg);
        return new TradeDetail(targetValue, tradeFee, tradeAmount, tradeServiceFee);
    }


    /**
     * 计算交易服务费
     *
     * @param tradeFee 交易费
     * @param tradeCfg 交易配置
     * @return 交易服务费
     */
    private static BigDecimal getTradeServiceFee(BigDecimal tradeFee, TradeCfg tradeCfg) {
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
        return tradeServiceFee;
    }


    /**
     * 解析Sma策略配置字符串并返回包含平均线列表和买入比例列表的Pair对象
     *
     * @param smaStrategyConfig Sma策略的配置字符串，格式为 "n|avg1,avg2,...,avgn|buyRatio1,buyRatio2,...,buyRatioN"
     * @return 包含平均线列表和买入比例列表的Pair对象
     * @throws IllegalArgumentException 如果输入的格式不正确或者平均线数量与买入比例数量不匹配，则抛出此异常
     */
    private Pair<List<Integer>, List<BigDecimal>> parseSmaStrategyPair(String smaStrategyConfig) {
        // 将配置字符串按"|"分割为数组
        String[] segments = smaStrategyConfig.split("\\|");
        // 如果数组长度不等于3，则抛出异常
        if (segments.length != 3) {
            throw new IllegalArgumentException("Invalid input format");
        }
        // 将数组的第一个元素解析为整数，代表平均线数量
        int n = Integer.parseInt(segments[0]);
        // 将数组的第二个元素按","分割为字符串数组，代表平均线列表
        String[] avgLines = segments[1].split(",");
        // 将数组的第三个元素按","分割为字符串数组，代表买入比例列表
        String[] buyRatios = segments[2].split(",");
        // 如果平均线数量或者买入比例数量与声明的数量不匹配，则抛出异常
        if (avgLines.length != n || buyRatios.length != n) {
            throw new IllegalArgumentException("The number of average lines or buy ratios does not match the declared number");
        }
        // 创建平均线列表和买入比例列表
        List<Integer> avgLineList = new ArrayList<>();
        List<BigDecimal> buyRatioList = new ArrayList<>();
        // 将平均线字符串数组中的每个元素转换为整数，并添加到平均线列表中
        for (String avgLine : avgLines) {
            avgLineList.add(Integer.parseInt(avgLine));
        }
        // 将买入比例字符串数组中的每个元素转换为BigDecimal对象，并添加到买入比例列表中
        for (String buyRatio : buyRatios) {
            buyRatioList.add(new BigDecimal(buyRatio));
        }
        // 返回包含平均线列表和买入比例列表的Pair对象
        return Pair.of(avgLineList, buyRatioList);
    }


    /**
     * 计算买入金额
     *
     * @param stock           股票信息
     * @param currentPrice    当前股价
     * @param smaStrategyPair 均线策略
     * @return 买入金额
     */
    private BigDecimal calculateIncrement(Stock stock, BigDecimal currentPrice, Pair<List<Integer>, List<BigDecimal>> smaStrategyPair) {
        TradeCfg tradeCfg = stock.getTradeCfg();
        // 默认步长
        BigDecimal increment = tradeCfg.getIncrement();
        // 基于估值水位调整步长
        increment = calculateIncrementByValuationRatio(tradeCfg, increment);

        // 注意：股价不是前复权时统计会有问题
        Date now = new Date();

        // 计算均线平均价格
        // 均线&价格
        final Map<Integer, BigDecimal> average = new HashMap<>(smaStrategyPair.getLeft().size());
        for (Integer averageVal : smaStrategyPair.getLeft()) {
            List<StockPrice> stockPrices = priceManager.getPrices(stock, now, averageVal);
            if (null == stockPrices || stockPrices.isEmpty()) {
                return increment;
            }
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (StockPrice stockPrice : stockPrices) {
                // 没有复权至就直接返回
                if (null == stockPrice.getRehabPrice()) {
                    return increment;
                }
                totalPrice = totalPrice.add(stockPrice.getRehabPrice());
            }
            average.put(averageVal, totalPrice.divide(BigDecimal.valueOf(stockPrices.size()), 4, RoundingMode.HALF_UP));
        }

        // 比较现价超过均线数量来决定浮动比例
        // 不用均线定比例的原因：下跌趋势过程中120均价>60均价>30均价>现价，股价突然上升，120均价>现价>60均价>30均价，这时现价低于120均线，大于60均价和30均价，使用120均线不合适。
        int overNum = -1;
        for (Integer averageVal : smaStrategyPair.getLeft()) {
            BigDecimal linePrice = average.get(averageVal);
            if (null != currentPrice && currentPrice.doubleValue() > linePrice.doubleValue()) {
                overNum++;
            }
        }
        //低于所有均线就进入下跌通道了，暂停买入。
        BigDecimal buyRatio = overNum == -1 ? BigDecimal.ZERO : smaStrategyPair.getRight().get(overNum);
        log.info("stock:{},overNum-1:{},multiplyVal:{}", stock.getCode(), overNum, buyRatio);
        increment = increment.multiply(buyRatio);
        return increment;
    }

    /**
     * 根据估值比率计算增量值
     *
     * @param tradeCfg  交易配置
     * @param increment 增量值
     * @return 计算后的增量值
     */
    private BigDecimal calculateIncrementByValuationRatio(TradeCfg tradeCfg, BigDecimal increment) {
        // 跟踪的指数估值
        String targetIndexCode = tradeCfg.getAttributes().get(TARGET_INDEX_CODE);
        if (null != targetIndexCode) {
            BigDecimal indexValuationRatio = priceManager.getIndexValuationRatio(targetIndexCode);
            if (null != indexValuationRatio) {
                // 冗余记录当前指数估值
                tradeCfg.getAttributes().put(CURRENT_TARGET_INDEX_VALUATION, indexValuationRatio.toPlainString());
                // 估值水位75%～100% 0倍
                // 估值水位50%～75%  0.5倍
                // 估值水位25%～50%  1倍
                // 估值水位 0%～25%  1.5倍
                if (indexValuationRatio.doubleValue() > 0.75) {
                    increment = BigDecimal.ZERO;
                } else if (indexValuationRatio.doubleValue() > 0.5) {
                    increment = increment.multiply(new BigDecimal("0.5"));
                } else if (indexValuationRatio.doubleValue() > 0.25) {
                    increment = increment.multiply(new BigDecimal("1"));
                } else if (indexValuationRatio.doubleValue() >= 0.0) {
                    increment = increment.multiply(new BigDecimal("1.5"));
                }
            }
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
            if (TradeType.BUY.equals(order.getType())) {
                // 减去手续费后实际用于购买份额的金额
                BigDecimal realTradeFee = tradeFee.subtract(tradeServiceFee);
                BigDecimal tradeAmount = realTradeFee.divide(settlementPrice, 2, RoundingMode.HALF_DOWN);
                return new TradeDetail(null, tradeFee, tradeAmount, tradeServiceFee);
            } else if (TradeType.SELL.equals(order.getType())) {
                // 暂不计算手续费
                BigDecimal tradeAmount = order.getTradeAmount();
                BigDecimal realTradeFee = tradeAmount.multiply(settlementPrice);
                return new TradeDetail(null, realTradeFee, tradeAmount, tradeServiceFee);
            }
        }
        return new TradeDetail(null, tradeFee, order.getTradeAmount(), tradeServiceFee);
    }
}
