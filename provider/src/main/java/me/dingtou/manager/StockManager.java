package me.dingtou.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import me.dingtou.constant.Status;
import me.dingtou.constant.StockType;
import me.dingtou.dao.StockDAO;
import me.dingtou.model.Stock;
import me.dingtou.util.StockConvert;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockManager {

    @Resource
    private StockDAO stockDAO;

    public Stock create(Stock stock) {
        if (null == stock
                || null == stock.getCode()
                || null == stock.getOwner()
                || null == stock.getType()) {
            return null;
        }
        me.dingtou.dataobject.Stock entity = StockConvert.convert(stock);
        int insert = stockDAO.insert(entity);
        if (insert == 1) {
            return queryById(entity.getId());
        }
        return null;
    }

    public Stock update(Stock stock) {
        if (null == stock
                || null == stock.getId()
                || null == stock.getCode()
                || null == stock.getOwner()
                || null == stock.getType()) {
            return null;
        }
        me.dingtou.dataobject.Stock entity = StockConvert.convert(stock);
        int update = stockDAO.updateById(entity);
        if (update == 1) {
            return queryById(stock.getId());
        }
        return null;

    }

    public List<Stock> query(String owner, StockType type) {
        if (null == owner) {
            return null;
        }
        QueryWrapper<me.dingtou.dataobject.Stock> query = new QueryWrapper<me.dingtou.dataobject.Stock>();
        query.eq("owner", owner);
        query.eq("status", Status.NORMAL.getCode());
        if (null != type) {
            query.eq("type", type.getCode());
        }
        query.orderByDesc("last_trade_time");
        query.orderByAsc("code");
        List<me.dingtou.dataobject.Stock> dbStockList = stockDAO.selectList(query);
        if (null == dbStockList || dbStockList.isEmpty()) {
            return null;
        }
        return dbStockList.stream()
                .map(e -> StockConvert.convert(e))
                .collect(Collectors.toList());
    }

    public Stock query(String owner, StockType type, String code) {
        if (null == owner || null == type || null == code) {
            return null;
        }
        QueryWrapper<me.dingtou.dataobject.Stock> query = new QueryWrapper<me.dingtou.dataobject.Stock>();
        query.eq("owner", owner);
        query.eq("status", Status.NORMAL.getCode());
        query.eq("type", type.getCode());
        query.eq("code", code);
        List<me.dingtou.dataobject.Stock> dbStockList = stockDAO.selectList(query);
        if (null == dbStockList || dbStockList.isEmpty()) {
            return null;
        }
        return dbStockList.stream()
                .map(e -> StockConvert.convert(e))
                .findFirst()
                .orElse(null);
    }

    public Stock queryById(Long id) {
        if (null == id) {
            return null;
        }
        me.dingtou.dataobject.Stock dbStock = stockDAO.selectById(id);
        return StockConvert.convert(dbStock);
    }
}
