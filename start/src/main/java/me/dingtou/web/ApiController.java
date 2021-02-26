package me.dingtou.web;

import com.alibaba.fastjson.JSON;
import me.dingtou.constant.Market;
import me.dingtou.constant.Status;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeCfg;
import me.dingtou.service.StockService;
import me.dingtou.service.TradeService;
import me.dingtou.strategy.trade.AverageValueTradeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

import static me.dingtou.strategy.trade.AverageValueTradeStrategy.AVERAGE_STRATEGY_KEY;
import static me.dingtou.strategy.trade.AverageValueTradeStrategy.CURRENT_TARGET_VALUE_KEY;

@RestController
public class ApiController {

    @Autowired
    private StockService stockService;

    @Autowired
    private TradeService tradeService;

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
     * @param owner
     * @param type
     * @param code
     * @param serviceFeeRate
     * @param minServiceFee
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/add", method = RequestMethod.GET)
    public Stock addStock(@RequestParam(value = "owner", required = false, defaultValue = "default") String owner,
                          @RequestParam(value = "type", required = true) String type,
                          @RequestParam(value = "code", required = true) String code,
                          @RequestParam(value = "market", required = true) String market,
                          @RequestParam(value = "increment", required = true) String increment,
                          @RequestParam(value = "serviceFeeRate", required = true) String serviceFeeRate,
                          @RequestParam(value = "minServiceFee", required = true) String minServiceFee,
                          @RequestParam(value = "minTradeAmount", required = true) String minTradeAmount)
            throws Exception {
        Stock stock = new Stock();
        stock.setOwner(owner);
        stock.setType(StockType.of(type));
        stock.setCode(code);
        stock.setMarket(Market.of(market));
        stock.setTradeStatus(TradeStatus.DONE);
        stock.setStatus(Status.NORMAL);

        TradeCfg tradeCfg = new TradeCfg();
        tradeCfg.setTradeStrategy(AverageValueTradeStrategy.CODE);
        tradeCfg.setTradeCron("0 0 15 ? * 6");
        tradeCfg.setIncrement(new BigDecimal(increment));
        tradeCfg.setServiceFeeRate(new BigDecimal(serviceFeeRate));
        tradeCfg.setMinServiceFee(new BigDecimal(minServiceFee));
        tradeCfg.setMinTradeAmount(new BigDecimal(minTradeAmount));

        Map<String, String> attributes = new HashMap<>();
        attributes.put(CURRENT_TARGET_VALUE_KEY, "0");

        Map<Integer, String> averageStrategy = new HashMap<>(4, 1.0f);
        // 默认1倍
        averageStrategy.put(0, "1");
        // 低于30日均线 目标增量1.5倍
        averageStrategy.put(30, "1.5");
        // 低于60日均线 目标增量2倍
        averageStrategy.put(60, "2.0");
        // 低于120日均线 目标增量2.5倍
        averageStrategy.put(120, "2.5");
        attributes.put(AVERAGE_STRATEGY_KEY, JSON.toJSONString(averageStrategy));

        tradeCfg.setAttributes(attributes);
        stock.setTradeCfg(tradeCfg);

        Stock dbStock = stockService.create(stock);
        return dbStock;
    }

    @RequestMapping(value = "/stock/query", method = RequestMethod.GET)
    public List<Stock> queryStock(@RequestParam(value = "owner", required = true, defaultValue = "default") String owner,
                                  @RequestParam(value = "type", required = false) String type)
            throws Exception {
        StockType stockType = null;
        if (null != type) {
            stockType = StockType.of(type);
        }
        List<Stock> stockList = stockService.query(owner, stockType);
        return stockList;
    }

    @RequestMapping(value = "/trade/conform", method = RequestMethod.GET)
    public List<Order> tradeConform(@RequestParam(value = "owner", required = true, defaultValue = "default") String owner)
            throws Exception {
        try {
            tradeService.settlement(owner);
        } catch (Throwable t) {
            //
        }
        List<Stock> stockList = stockService.query(owner, null);
        if (null == stockList || stockList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Order> orderList = new ArrayList<>();
        for (Stock stock : stockList) {
            Order conform = tradeService.conform(stock.getOwner(), stock.getType(), stock.getCode());
            orderList.add(conform);
        }
        return orderList;
    }

    @RequestMapping(value = "/trade/buy", method = RequestMethod.POST)
    public List<Order> tradeBuy(@RequestParam(value = "owner", required = true, defaultValue = "default") String owner,
                                @RequestParam(value = "orders", required = true) String orders)
            throws Exception {
        List<Order> orderList = JSON.parseArray(orders, Order.class);

        List<Order> resultOrders = new ArrayList<>();
        for (Order order : orderList) {
            if (!owner.equals(order.getStock().getOwner())) {
                continue;
            }
            Order buyOrder = tradeService.buy(order);
            resultOrders.add(buyOrder);
        }
        return resultOrders;
    }

    @RequestMapping(value = "/trade/settlement", method = RequestMethod.GET)
    public List<Order> tradeSettlement(@RequestParam(value = "owner", required = true, defaultValue = "default") String owner)
            throws Exception {
        return tradeService.settlement(owner);
    }
}
