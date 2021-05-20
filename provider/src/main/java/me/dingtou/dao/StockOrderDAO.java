package me.dingtou.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.dingtou.dataobject.StockOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StockOrderDAO extends BaseMapper<StockOrder> {

    @Select("select * from stock_order where stock_id = #{stockId} order by trade_time asc limit #{start}, #{pageSize}")
    List<StockOrder> selectByPage(@Param("stockId") Long stockId, @Param("start") Integer start, @Param("pageSize") Integer pageSize);
}
