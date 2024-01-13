package me.dingtou.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.dingtou.constant.StockType;
import me.dingtou.manager.PriceManager;
import me.dingtou.manager.StockManager;
import me.dingtou.model.Asset;
import me.dingtou.model.Stock;
import me.dingtou.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_DOWN;

@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockManager stockManager;

    @Autowired
    private PriceManager priceManager;

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

    @Override
    public List<Asset> statistics(String owner) {
        List<Stock> stocks = stockManager.query(owner, null);
        if (null == stocks || stocks.isEmpty()) {
            return Collections.emptyList();
        }
        // 初始化名字&总金额
        List<Asset> assetList = stocks.parallelStream().map(e -> {
            Asset asset = new Asset();
            asset.setAmount(e.getAmount());
            String name = e.getName();
            if (null == name) {
                name = e.getCode();
            }
            asset.setName(name);
            String category = e.getCategory();
            if (null == category) {
                category = "未分类";
            }
            asset.setCategory(category);
            String subCategory = e.getSubCategory();
            if (null == subCategory) {
                subCategory = "未分类";
            }
            asset.setSubCategory(subCategory);
            asset.setCode(e.getCode());
            BigDecimal currentPrice = null;
            try {
                currentPrice = priceManager.getCurrentPrice(e);
            } catch (Exception ex) {
                currentPrice = BigDecimal.ZERO;
            }
            asset.setCurrentPrice(currentPrice);
            asset.setTotalFee(currentPrice.multiply(e.getAmount()).setScale(2, ROUND_DOWN));
            return asset;
        }).collect(Collectors.toList());

        // 计算总金额
        BigDecimal totalAssetFee = new BigDecimal(0);
        for (int i = 0; i < assetList.size(); i++) {
            Asset asset = assetList.get(i);
            totalAssetFee = totalAssetFee.add(asset.getTotalFee());
        }

        // 分摊比例
        BigDecimal totalRatio = new BigDecimal(0);
        for (int i = 0; i < assetList.size(); i++) {
            Asset asset = assetList.get(i);
            if (i == assetList.size() - 1) {
                asset.setRatio(BigDecimal.ONE.subtract(totalRatio));
                break;
            }
            BigDecimal ratio = asset.getTotalFee().divide(totalAssetFee, 4, BigDecimal.ROUND_HALF_UP);
            totalRatio = totalRatio.add(ratio);
            asset.setRatio(ratio);
        }
        return assetList;
    }

}
