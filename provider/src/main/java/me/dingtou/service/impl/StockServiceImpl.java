package me.dingtou.service.impl;

import me.dingtou.constant.StockType;
import me.dingtou.manager.StockManager;
import me.dingtou.model.Stock;
import me.dingtou.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    @Resource
    private StockManager stockManager;

    @Override
    public Stock create(Stock stock) {
        return stockManager.create(stock);
    }

    @Override
    public Stock update(Stock stock) {
        return stockManager.update(stock);
    }

    @Override
    public List<Stock> query(String owner) {
        return stockManager.query(owner, null);
    }

    @Override
    public List<Stock> query(String owner, StockType type) {
        return stockManager.query(owner, type);
    }

    @Override
    public Stock query(String owner, StockType type, String code) {
        return stockManager.query(owner, type, code);
    }
}
