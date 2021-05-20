package me.dingtou.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import me.dingtou.login.utils.UserUtils;
import me.dingtou.model.ChartView;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;
import me.dingtou.model.StockOrder;
import me.dingtou.model.StockPackage;
import me.dingtou.result.AppPageResult;
import me.dingtou.result.AppResult;
import me.dingtou.service.DataService;
import me.dingtou.service.StockService;
import me.dingtou.service.TradeService;
import me.dingtou.utils.OrderUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ken
 */
@RestController()
@RequestMapping("/api")
public class XApiController {
    @Resource
    private ApiController apiController;

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
     * @param param
     * @return
     */
    @RequestMapping(value = "/stock/add", method = RequestMethod.POST)
    public AppResult addStock(@RequestBody String param) throws Exception {
        JSONObject object = JSON.parseObject(param);
        String type = object.getString("type");
        String code = object.getString("code");
        String name = object.getString("name");
        String category = object.getString("category");
        String market = object.getString("market");
        String increment = object.getString("increment");
        String serviceFeeRate = object.getString("serviceFeeRate");
        String minServiceFee = object.getString("minServiceFee");
        String minTradeAmount = object.getString("minTradeAmount");

        try {
            Stock stock = apiController.addStock(type, code, name, category, market, increment, serviceFeeRate, minServiceFee, minTradeAmount);
            return AppResult.success(stock);
        } catch (DuplicateKeyException e) {
            String message = "不能重复添加证券：".concat("代码:").concat(code).concat(", 名称:").concat(name);
            return AppResult.fail("ADD_STOCK_DUPLICATE", message);
        } catch (Throwable e){
            return AppResult.fail("ADD_STOCK_ERROR", e.getMessage());
        }
    }

    @RequestMapping(value = "/stock/update",  method = RequestMethod.POST)
    public AppResult updateStock(@RequestBody String param){

        try {
            Stock stock = JSON.parseObject(param, Stock.class);
            stock = stockService.update(stock);

            return Optional.ofNullable(stock)
                .map(AppResult::success)
                .orElse(AppResult.fail("UPDATE_STOCK_ERROR", "更新异常"));
        } catch (Throwable e){
            return AppResult.fail("UPDATE_STOCK_ERROR", e.getMessage());
        }
    }

    @RequestMapping(value = "/stock/query", method = RequestMethod.GET)
    public List<Stock> queryStock(@RequestParam(value = "type", required = false) String type) throws Exception {

        return apiController.queryStock(type);
    }

    @RequestMapping(value = "/stock/queryById", method = RequestMethod.GET)
    public AppResult<Stock> queryStockById(@RequestParam(value = "id", required = false) String id) throws Exception {

        Stock stock = stockService.query(Long.parseLong(id));

        return Optional.ofNullable(stock)
            .filter(x->StringUtils.equals(x.getOwner(), UserUtils.userName()))
            .map(AppResult::success)
            .orElse(AppResult.fail());
    }

    @RequestMapping(value = "/trade/conform", method = RequestMethod.GET)
    public AppResult<List<JSONObject>> tradeConformPage(@RequestParam(value = "sorter", required = false) String sorter,
            @RequestParam(value = "stockCode", required = false) String stockCode,
            @RequestParam(value = "stock&name", required = false) String stockName)
            throws Exception {
        final JSONObject sorterJson = StringUtils.isNotEmpty(sorter)?JSON.parseObject(sorter):new JSONObject();

        List<Order> list = apiController.tradeConform();
        if(CollectionUtils.isEmpty(list)){
            return AppResult.success();
        }

        String totalFeeLabel = "stock&totalFee";
        String totalAmountLabel = "stock&amount";
        String tradeAmountLabel = "tradeAmount";
        String tradeFeeLabel = "tradeFee";
        String tradeServiceFeeLabel = "tradeServiceFee";
        String stockMarketLabel = "stockMarket";
        String typeLabel = "type";
        String statusLabel = "status";
        String ascendSort = "ascend";

        List<JSONObject> objects = list.stream()
            .filter(x-> Objects.nonNull(x.getStock()))
            .filter(x-> {
                //搜索逻辑处理
               if(StringUtils.isEmpty(stockCode)){
                   return true;
               } else {
                   return StringUtils.equals(stockCode, x.getStock().getCode());
               } })
            .filter(x-> {
                //搜索逻辑处理
                if(StringUtils.isEmpty(stockName)){
                    return true;
                } else {
                    return StringUtils.indexOf(x.getStock().getName(), stockName)>=0;
                } })
            .map(OrderUtils::orderConver)
            .sorted((JSONObject o1, JSONObject o2)->{
                //排序逻辑处理
                if(sorterJson.containsKey(totalFeeLabel)){
                    return compareTo(totalFeeLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(totalAmountLabel)){
                    return compareTo(totalAmountLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(typeLabel)){
                    return compareTo(typeLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(tradeAmountLabel)){
                    return compareTo(tradeAmountLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(tradeFeeLabel)){
                    return compareTo(tradeFeeLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(tradeServiceFeeLabel)){
                    return compareTo(tradeServiceFeeLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(statusLabel)){
                    return compareTo(statusLabel, ascendSort, sorterJson, o1, o2);
                } else if(sorterJson.containsKey(stockMarketLabel)){
                    return compareTo(stockMarketLabel, ascendSort, sorterJson, o1, o2);
                }

                return 0;
            })
            .collect(Collectors.toList());

        return AppResult.success(objects);
    }

    private int compareTo(String totalAmount, String ascend, JSONObject sorterJson, JSONObject o1, JSONObject o2) {
        return StringUtils.equals(sorterJson.getString(totalAmount), ascend) ?
            o1.getString(totalAmount).compareTo(o2.getString(totalAmount))
            : o2.getString(totalAmount).compareTo(o1.getString(totalAmount));
    }

    @RequestMapping(value = "/trade/buy", method = RequestMethod.POST)
    public AppResult<List<Order>> tradeBuy(@RequestBody String params) throws Exception {
        try {
            String orders = JSON.parseObject(params).getString("orders");
            List<Order> list = apiController.tradeBuy(orders);
            return AppResult.success(list);
        } catch (Throwable e){
            return AppResult.fail("TRADE_BUY_ERROR", e.getMessage());
        }
    }

    @RequestMapping(value = "/calculate/adjust", method = RequestMethod.POST)
    public AppResult tradeUpdate(@RequestBody String params) throws Exception {
        try {
            JSONObject data = JSON.parseObject(params);

            JSONObject orderJson = Optional.of(data)
                .map(x->x.getJSONObject("order"))
                .orElse(null);

            Order oldOrder = orderJson.toJavaObject(Order.class);

            Order order = OrderUtils.orderConver(orderJson);
            if(Objects.isNull(order) || Objects.isNull(order.getStock())
                || Objects.isNull(oldOrder) || Objects.isNull(oldOrder.getStock())){
                throw new RuntimeException("参数异常!");
            }

            Stock newStock = order.getStock();

            if(Objects.isNull(newStock.getAmount()) || newStock.getAmount().compareTo(BigDecimal.ZERO)<=0){
                throw new RuntimeException("数量必须大于0!");
            }

            BigDecimal tradeFee = newStock.getTotalFee().subtract(oldOrder.getStock().getTotalFee());
            BigDecimal tradeAmount = newStock.getAmount().subtract(oldOrder.getStock().getAmount());

            BigDecimal serviceFee = Optional.ofNullable(order.getTradeServiceFee())
                .filter(x->Objects.nonNull(oldOrder.getTradeServiceFee()))
                .map(x->x.subtract(oldOrder.getTradeServiceFee()))
                .orElse(order.getTradeServiceFee());

            if(Objects.isNull(serviceFee)){
                serviceFee = BigDecimal.valueOf(0);
            }

            if(tradeFee.compareTo(BigDecimal.ZERO)==0
                && tradeAmount.compareTo(BigDecimal.ZERO) == 0
                && serviceFee.compareTo(BigDecimal.ZERO) == 0){
                throw new RuntimeException("数据无变更!");
            }

            Order result = apiController.tradeAdjust(newStock.getType().getCode(), newStock.getCode(), tradeFee.toString(), tradeAmount.toString(), serviceFee.toString());
            apiController.redoStatistic();
            if (Objects.isNull(result)) {
                throw new RuntimeException("更新数据异常");
            }
            return AppResult.success();
        } catch (Throwable e){
            return AppResult.fail("TRADE_UPDATE_ERROR", e.getMessage());
        }
    }

    @RequestMapping(value = "/trade/adjust", method = RequestMethod.POST)
    public Order tradeAdjust(@RequestParam(value = "type", required = true) String type,
                             @RequestParam(value = "code", required = true) String code,
                             @RequestParam(value = "tradeFee", required = true) String tradeFeeInput,
                             @RequestParam(value = "tradeAmount", required = true) String tradeAmountInput,
                             @RequestParam(value = "tradeServiceFee", required = true) String tradeServiceFeeInput)
            throws Exception {
        return apiController.tradeAdjust(type, code, tradeFeeInput, tradeAmountInput, tradeServiceFeeInput);
    }

    @RequestMapping(value = "/trade/settlement", method = RequestMethod.GET)
    public List<Order> tradeSettlement() throws Exception {
       return apiController.tradeSettlement();
    }

    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
    public Boolean redoStatistic()
            throws Exception {
        return apiController.redoStatistic();
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public StockPackage exportData()
            throws Exception {
        return apiController.exportData();
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public boolean importData(@RequestParam(value = "data", required = true) String data)
            throws Exception {
        return apiController.importData(data);
    }

    @RequestMapping(value = "/stock/statisticsDetailView", method = RequestMethod.GET)
    public List<ChartView> stockStatisticsDetailView() throws Exception {
        return apiController.stockStatisticsDetailView();
    }


    /**
     * http://127.0.0.1:8080/stock/statisticsCategoryView?owner=allFund
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stock/statisticsCategoryView", method = RequestMethod.GET)
    public List<ChartView> stockStatisticsCategoryView() throws Exception {
        return apiController.stockStatisticsCategoryView();
    }

    @RequestMapping(value = "/order/queryStockOrderByStockId", method = RequestMethod.GET)
    public AppPageResult<StockOrder> queryStockOrder(@RequestParam(value = "stockId", required = true) Long stockId,
        @RequestParam(value = "current", required = true) int current,
        @RequestParam(value = "pageSize", required = true) int pageSize) throws Exception {
        return tradeService.queryStockOrder(UserUtils.userName(), stockId, current, pageSize);
    }

    @RequestMapping(value = "/order/updateStockOrderByStockId", method = RequestMethod.POST)
    public AppResult<Integer> updateStockOrder(@RequestBody String params) throws Exception {
        JSONObject data = JSON.parseObject(params);
        JSONObject orderJson = Optional.of(data)
            .map(x->x.getJSONObject("stockOrder"))
            .orElse(null);

        StockOrder stockOrder = orderJson.toJavaObject(StockOrder.class);
        Integer integer = tradeService.updateStockOrder(UserUtils.userName(), stockOrder);
        return AppResult.success(integer);
    }

    @RequestMapping(value = "/order/deleteStockOrderByStockId", method = RequestMethod.GET)
    public AppResult<Integer> deleteStockOrderByStockId(@RequestParam(value = "id", required = true) Long id) throws Exception {
        Integer integer =  tradeService.deleteStockOrder(UserUtils.userName(), id);
        return AppResult.success(integer);
    }

}
