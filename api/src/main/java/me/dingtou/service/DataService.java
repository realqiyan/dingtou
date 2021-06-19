package me.dingtou.service;

import me.dingtou.model.StockPackage;

/**
 * 数据服务
 */
public interface DataService {

    /**
     * 导出数据
     *
     * @param owner
     * @return
     */
    StockPackage exportData(String owner);

    /**
     * 导入数据
     *
     *
     * @param owner
     * @param data
     * @return
     */
    boolean importData(String owner, StockPackage data);

}
