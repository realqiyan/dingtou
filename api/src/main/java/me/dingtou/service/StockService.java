package me.dingtou.service;

import me.dingtou.constant.StockType;
import me.dingtou.model.Asset;
import me.dingtou.model.Stock;

import java.util.List;

/**
 * 交易服务
 */
public interface StockService {

    /**
     * 创建证券
     *
     * @param stock
     * @return
     */
    Stock create(Stock stock);


    /**
     * 更新证券
     *
     * @param stock
     * @return
     */
    Stock update(Stock stock);


    /**
     * 查询证券
     *
     * @param owner
     * @return
     */
    List<Stock> query(String owner);


    /**
     * 查询证券
     *
     * @param owner
     * @param type
     * @return
     */
    List<Stock> query(String owner, StockType type);


    /**
     * 查询证券
     *
     * @param owner
     * @param type
     * @param code
     * @return
     */
    Stock query(String owner, StockType type, String code);


    /**
     * 统计资产占比
     *
     * @param owner
     * @return
     */
    List<Asset> statistics(String owner);
}
