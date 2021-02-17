package me.dingtou.util;

import me.dingtou.constant.TradeStatus;
import me.dingtou.constant.TradeType;
import me.dingtou.dataobject.StockOrder;
import me.dingtou.model.Order;
import me.dingtou.model.Stock;

public class OrderConvert {
    public static Order convert(Stock stock, StockOrder dbOrder) {
        if (null == stock || null == dbOrder) {
            return null;
        }
        Order order = new Order();
        order.setStock(stock);
        order.setTradeAmount(dbOrder.getTradeAmount());
        order.setTradeFee(dbOrder.getTradeFee());
        order.setType(TradeType.of(dbOrder.getType()));
        order.setStatus(TradeStatus.of(dbOrder.getTradeStatus()));
        order.setTradeServiceFee(dbOrder.getTradeServiceFee());
        order.setCreateTime(dbOrder.getCreateTime());
        order.setTradeTime(dbOrder.getTradeTime());
        order.setOutId(dbOrder.getOutId());
        order.setOrderId(dbOrder.getId());
        return order;
    }
}
