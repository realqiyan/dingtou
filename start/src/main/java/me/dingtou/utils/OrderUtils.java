package me.dingtou.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import me.dingtou.model.Order;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 上叶
 * @date 2021/5/18
 **/
public class OrderUtils {
    public final static String target = "stock";
    public final static String prefix = target+"&";

    public static JSONObject orderConver(Order order){
        if(Objects.isNull(order)){
            return null;
        }

        JSONObject object = JSON.parseObject(JSON.toJSONString(order));
        JSONObject stock = object.getJSONObject(target);

        Optional.ofNullable(stock)
            .map(JSONObject::keySet)
            .ifPresent(ks-> ks.forEach(x->object.put(prefix+x, stock.get(x))));

        if(Objects.nonNull(order.getStock().getMarket())) {
            object.put(prefix + "marketName", order.getStock().getMarket().getName());
        }
        if(Objects.nonNull(order.getStock().getType())) {
            object.put(prefix + "typeName", order.getStock().getType().getName());
        }
        return object;
    }

    public static Order orderConver(JSONObject jsonObject){
        if(Objects.isNull(jsonObject)){
            return null;
        }

        Optional.of(jsonObject)
            .filter(x->x.containsKey(target))
            .map(JSONObject::keySet)
            .map(x->x.stream().filter(k->StringUtils.startsWith(k, prefix)).collect(Collectors.toList()))
            .ifPresent(x->{
                JSONObject stock = jsonObject.getJSONObject(target);
                x.forEach(k->{
                    Object value = jsonObject.get(k);
                    String key = k.replace(prefix, "");
                    if(Objects.nonNull(value)){
                        stock.put(key, value);
                    }
                });
            });

        return jsonObject.toJavaObject(Order.class);
    }
}
