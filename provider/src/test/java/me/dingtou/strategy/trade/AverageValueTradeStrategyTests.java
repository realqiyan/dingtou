package me.dingtou.strategy.trade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.dingtou.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AverageValueTradeStrategyTests {
    public static void main(String[] args) {
        test_qfq();
    }

    private static void test_qfq() {

        //前复权:https://finance.sina.com.cn/realstock/company/sz000002/qfq.js
        String adjustApiUrl = String.format("https://finance.sina.com.cn/realstock/company/%s/qfq.js", "sz000002");
        String adjustContent = HttpUtils.getUrlContent(adjustApiUrl);
        if (!StringUtils.isBlank(adjustContent)) {
            Pattern qfq = Pattern.compile("(\\{.*\\})");
            Matcher m = qfq.matcher(adjustContent);
            if (m.find()) {
                JSONObject adjustJson = JSON.parseObject(m.group(0));
                System.out.println(adjustJson);
            }
        }

    }
}
