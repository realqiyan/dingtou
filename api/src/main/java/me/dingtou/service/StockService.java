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
     * @param stock 证券对象
     * @return 创建的证券对象
     */
    Stock create(Stock stock);

    /**
     * 更新证券
     *
     * @param stock 需要更新的证券对象
     * @return 更新后的证券对象
     */
    Stock update(Stock stock);

    /**
     * 查询证券
     *
     * @param owner 所有者的名称
     * @return 所有者所拥有的证券列表
     */
    List<Stock> query(String owner);

    /**
     * 查询证券
     *
     * @param owner  所有者的名称
     * @param type    证券的类型
     * @return 所有者所拥有的指定类型的证券列表
     */
    List<Stock> query(String owner, StockType type);

    /**
     * 查询证券
     *
     * @param owner  所有者的名称
     * @param type   证券的类型
     * @param code   证券的代码
     * @return 所有者所拥有的指定类型和代码的证券对象
     */
    Stock query(String owner, StockType type, String code);

    /**
     * 统计资产占比
     *
     * @param owner 所有者的名称
     * @return 所有者的资产占比列表
     */
    List<Asset> statistics(String owner);

}
