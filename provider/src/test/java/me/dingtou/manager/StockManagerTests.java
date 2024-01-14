package me.dingtou.manager;

import com.alibaba.fastjson.JSON;
import me.dingtou.model.TradeCfg;
import me.dingtou.strategy.trade.AverageValueTradeStrategy;

import java.math.BigDecimal;

class StockManagerTests {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void create() {
    }

    @org.junit.jupiter.api.Test
    void update() {
    }

    @org.junit.jupiter.api.Test
    void query() {
    }

    @org.junit.jupiter.api.Test
    void queryById() {
    }

    public static void main(String[] args) {
        TradeCfg tradeCfg = new TradeCfg();
        tradeCfg.setIncrement(BigDecimal.valueOf(500));
        tradeCfg.setMinServiceFee(BigDecimal.ZERO);
        tradeCfg.setServiceFeeRate(new BigDecimal("0.0015"));
        tradeCfg.setTradeStrategy(AverageValueTradeStrategy.CODE);
        System.out.println(JSON.toJSONString(tradeCfg));
    }
}