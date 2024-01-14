package me.dingtou.service;

import me.dingtou.model.StockPackage;

/**
 * 数据服务
 */
public interface DataService {
    /**
     * 导出数据
     *
     * @param owner 所有者
     * @return 导出的数据包
     */
    StockPackage exportData(String owner);

    /**
     * 导入数据
     *
     * @param owner   所有者
     * @param data    导入的数据包
     * @return 是否导入成功
     */
    boolean importData(String owner, StockPackage data);

}
