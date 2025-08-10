package me.dingtou.web;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.*;
import me.dingtou.model.*;
import me.dingtou.service.DataService;
import me.dingtou.service.StockService;
import me.dingtou.service.TradeService;
import me.dingtou.strategy.trade.AverageValueTradeStrategy;
import me.dingtou.web.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static me.dingtou.strategy.trade.AverageValueTradeStrategy.*;

@Slf4j
@RestController
public class ApiController {

    @Autowired
    private StockService stockService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private DataService dataService;

    /**
     * <pre>
     * http://127.0.0.1:8080/stock/add?type=fund&code=005827&increment=500&serviceFeeRate=0.0015&minServiceFee=0&market=fund&minTradeAmount=0.01
     * http://127.0.0.1:8080/stock/add?type=stock&code=510300&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sh&minTradeAmount=100
     * http://127.0.0.1:8080/stock/add?type=stock&code=510500&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sh&minTradeAmount=100
     * http://127.0.0.1:8080/stock/add?type=stock&code=510900&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sh&minTradeAmount=100
     * http://127.0.0.1:8080/stock/add?type=stock&code=159905&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sz&minTradeAmount=100
     * http://127.0.0.1:8080/stock/add?type=stock&code=515180&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sh&minTradeAmount=100
     * </pre>
     *
     * @param type
     * @param code
     * @param serviceFeeRate
     * @param minServiceFee
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/add", method = RequestMethod.GET)
    public Stock addStock(@RequestParam(value = "type", required = true) String type,
                          @RequestParam(value = "code", required = true) String code,
                          @RequestParam(value = "market", required = true) String market,
                          @RequestParam(value = "increment", required = true) String increment,
                          @RequestParam(value = "serviceFeeRate", required = true) String serviceFeeRate,
                          @RequestParam(value = "minServiceFee", required = true) String minServiceFee,
                          @RequestParam(value = "targetIndexCode", required = true) String targetIndexCode,
                          @RequestParam(value = "minTradeAmount", required = true) String minTradeAmount)
            throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        Stock stock = new Stock();
        stock.setOwner(owner);
        stock.setType(StockType.of(type));
        stock.setCode(code);
        stock.setMarket(Market.of(market));
        stock.setTradeStatus(TradeStatus.DONE);
        stock.setStatus(Status.NORMAL);

        TradeCfg tradeCfg = new TradeCfg();
        tradeCfg.setTradeStrategy(AverageValueTradeStrategy.CODE);
        tradeCfg.setIncrement(new BigDecimal(increment));
        tradeCfg.setServiceFeeRate(new BigDecimal(serviceFeeRate));
        tradeCfg.setMinServiceFee(new BigDecimal(minServiceFee));
        tradeCfg.setMinTradeAmount(new BigDecimal(minTradeAmount));

        Map<String, String> attributes = new HashMap<>();
        attributes.put(CURRENT_TARGET_VALUE_KEY, "0");

        attributes.put(SMA_STRATEGY_KEY, "4|10,30,60,120|1.5,1.25,1,0");

        //目标指数
        attributes.put(TARGET_INDEX_CODE, targetIndexCode);

        tradeCfg.setAttributes(attributes);
        stock.setTradeCfg(tradeCfg);

        stock.setTotalFee(BigDecimal.ZERO);
        stock.setAmount(BigDecimal.ZERO);

        return stockService.create(stock);
    }

    @RequestMapping(value = "/stock/query", method = RequestMethod.GET)
    public List<Stock> queryStock(@RequestParam(value = "type", required = false) String type) throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        StockType stockType = null;
        if (null != type) {
            stockType = StockType.of(type);
        }
        List<Stock> stockList = stockService.query(owner, stockType);
        return stockList;
    }


    /**
     * http://127.0.0.1:8080/stock/statistics?owner=allFund
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/statistics", method = RequestMethod.GET)
    public List<Asset> stockStatistics() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        List<Asset> assetList = stockService.statistics(owner);
        return assetList;
    }

    /**
     * http://127.0.0.1:8080/stock/statisticsDetailView?owner=allFund
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/statisticsDetailView", method = RequestMethod.GET)
    public List<ChartView> stockStatisticsDetailView() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        List<Asset> assetList = stockService.statistics(owner);
        return assetList.stream()
                .map(asset -> buildAssetChartView(asset))
                .sorted(Comparator.comparing(ChartView::getName))
                .collect(Collectors.toList());
    }


    /**
     * http://127.0.0.1:8080/stock/statisticsCategoryView?owner=allFund
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/statisticsCategoryView", method = RequestMethod.GET)
    public List<ChartView> stockStatisticsCategoryView() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        List<Asset> assetList = stockService.statistics(owner);

        // 分组
        Map<String, List<Asset>> groupMap = assetList.stream().collect(Collectors.groupingBy(Asset::getCategory));

        // 聚合数据
        List<ChartView> categoryChartViewList = new ArrayList<>();
        BigDecimal multiplicand = BigDecimal.valueOf(groupMap.size());
        for (Iterator<Map.Entry<String, List<Asset>>> i = groupMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, List<Asset>> next = i.next();
            String category = next.getKey();
            List<Asset> assets = next.getValue();

            List<ChartView> children = new ArrayList<>();
            BigDecimal totalRatio = BigDecimal.ZERO;
            for (Asset asset : assets) {
                totalRatio = totalRatio.add(asset.getRatio());
                ChartView assetChartView = buildAssetChartView(asset);
                assetChartView.setValue(asset.getRatio().multiply(multiplicand).toPlainString());
                children.add(assetChartView);
            }
            String ratio = totalRatio.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString();
            ChartView categoryChartView = new ChartView();
            categoryChartView.setName(category + '-' + ratio + '%');
            String value = totalRatio.multiply(multiplicand).toPlainString();
            categoryChartView.setValue(value);
            categoryChartView.setChildren(children);
            categoryChartViewList.add(categoryChartView);
        }
        categoryChartViewList.sort(Comparator.comparing(ChartView::getName));

        return categoryChartViewList;
    }

    /**
     * 通过资产构建视图
     *
     * @param asset
     * @return
     */
    private ChartView buildAssetChartView(Asset asset) {
        ChartView chartView = new ChartView();
        String ratio = asset.getRatio().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toPlainString();
        chartView.setName(asset.getCategory() + '-' + asset.getName() + '-' + ratio + '%');
        chartView.setValue(asset.getTotalFee().toPlainString());
        return chartView;
    }


    @RequestMapping(value = "/trade/conform", method = RequestMethod.GET)
    public List<Order> tradeConform() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        try {
            tradeService.settlement(owner);
        } catch (Throwable t) {
            log.error("settlement error,message:" + t.getMessage());
        }
        List<Stock> stockList = stockService.query(owner, null);
        if (null == stockList || stockList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Order> orderList = new ArrayList<>();
        for (Stock stock : stockList) {
            if (null == stock.getTradeCfg() || null == stock.getTradeCfg().getTradeStrategy()) {
                continue;
            }
            Order conform = tradeService.conform(stock.getOwner(), stock.getType(), stock.getCode());
            orderList.add(conform);
        }
        return orderList;
    }

    @RequestMapping(value = "/trade/buy", method = RequestMethod.POST)
    public List<Order> tradeBuy(@RequestParam(value = "outIds", required = true) String outIds,
                                @RequestParam(value = "orders", required = true) String orders) throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        List<String> outIdList = JSON.parseArray(outIds, String.class);
        List<Order> orderList = JSON.parseArray(orders, Order.class);

        List<Order> resultOrders = new ArrayList<>();
        for (Order order : orderList) {
            if (!owner.equals(order.getStock().getOwner()) || !outIdList.contains(order.getOutId())) {
                continue;
            }
            Order buyOrder = tradeService.buy(order);
            resultOrders.add(buyOrder);
        }
        return resultOrders;
    }

    @RequestMapping(value = "/trade/adjust", method = RequestMethod.POST)
    public Order tradeAdjust(@RequestParam(value = "type", required = true) String type,
                             @RequestParam(value = "code", required = true) String code,
                             @RequestParam(value = "tradeFee", required = true) String tradeFeeInput,
                             @RequestParam(value = "tradeAmount", required = true) String tradeAmountInput,
                             @RequestParam(value = "tradeServiceFee", required = true) String tradeServiceFeeInput)
            throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        BigDecimal tradeFee = new BigDecimal(tradeFeeInput);
        BigDecimal tradeAmount = new BigDecimal(tradeAmountInput);
        BigDecimal tradeServiceFee = new BigDecimal(tradeServiceFeeInput);
        Stock stock = stockService.query(owner, StockType.of(type), code);
        if (null == stock) {
            return null;
        }
        Order order = new Order();
        order.setStock(stock);
        order.setType(TradeType.ADJUST);
        order.setStatus(TradeStatus.DONE);
        Date now = new Date();
        order.setTradeTime(now);
        order.setCreateTime(now);
        order.setOutId(String.valueOf(System.currentTimeMillis()));
        order.setTradeFee(tradeFee);
        order.setTradeAmount(tradeAmount);
        order.setTradeServiceFee(tradeServiceFee);
        return tradeService.adjust(order);
    }

    @RequestMapping(value = "/trade/auto-adjust", method = RequestMethod.POST)
    public List<Order> tradeAutoAdjust() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        return tradeService.autoAdjust(owner);
    }

    @RequestMapping(value = "/trade/settlement", method = RequestMethod.GET)
    public List<Order> tradeSettlement() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        return tradeService.settlement(owner);
    }

    @RequestMapping(value = "/trade/statistic", method = RequestMethod.GET)
    public Boolean redoStatistic() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        return tradeService.statistic(owner);
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public StockPackage exportData() throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        return dataService.exportData(owner);
    }


    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public boolean importData(@RequestParam(value = "data", required = true) String data) throws Exception {
        String owner = SessionUtils.getCurrentOwner();
        StockPackage stockPackage = JSON.parseObject(data, StockPackage.class);

        return dataService.importData(owner, stockPackage);
    }
}
