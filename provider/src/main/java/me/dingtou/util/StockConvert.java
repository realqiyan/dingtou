package me.dingtou.util;

import me.dingtou.constant.Market;
import me.dingtou.constant.Status;
import me.dingtou.constant.StockType;
import me.dingtou.constant.TradeStatus;
import me.dingtou.model.Stock;
import me.dingtou.model.TradeCfg;

/**
 * 对象转换
 *
 * @author yuanhongbo
 */
public class StockConvert {
    /**
     * convert
     *
     * @param stock
     * @return
     */
    public static Stock convert(me.dingtou.dataobject.Stock stock) {
        Stock newStock = new Stock();
        newStock.setId(stock.getId());
        newStock.setCode(stock.getCode());
        newStock.setOwner(stock.getOwner());
        newStock.setTotalFee(stock.getTotalFee());
        newStock.setAmount(stock.getAmount());
        newStock.setLastTradeTime(stock.getLastTradeTime());
        newStock.setTradeCfg(TradeCfg.of(stock.getTradeCfg()));
        newStock.setMarket(Market.of(stock.getMarket()));
        newStock.setType(StockType.of(stock.getType()));
        newStock.setTradeStatus(TradeStatus.of(stock.getTradeStatus()));
        newStock.setStatus(Status.of(stock.getStatus()));
        return newStock;
    }

    /**
     * convert
     *
     * @param stock
     * @return
     */
    public static me.dingtou.dataobject.Stock convert(Stock stock) {
        me.dingtou.dataobject.Stock newStock = new me.dingtou.dataobject.Stock();
        newStock.setId(stock.getId());
        newStock.setCode(stock.getCode());
        newStock.setOwner(stock.getOwner());
        newStock.setTotalFee(stock.getTotalFee());
        newStock.setAmount(stock.getAmount());
        newStock.setLastTradeTime(stock.getLastTradeTime());
        newStock.setTradeCfg(stock.getTradeCfg().toString());
        newStock.setMarket(stock.getMarket().getCode());
        newStock.setType(stock.getType().getCode());
        newStock.setTradeStatus(stock.getTradeStatus().getCode());
        newStock.setStatus(stock.getStatus().getCode());
        return newStock;
    }
}
